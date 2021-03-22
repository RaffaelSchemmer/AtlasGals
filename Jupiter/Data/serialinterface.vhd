--#############################################################################
--
--   módulo serial com autobaud - versão sem CTS nem RTS!
--  
--    clock     -- clock
--    reset     -- reset (ativo em 1)
--
--    rx_data   -- byte a ser transmitido para o pc
--    rx_start  -- indica byte disponivel no rx_data (ativo em 1) 
--    rx_busy   -- fica em '1' enquando envia ao PC (do rx_start ao fim)
--    rxd       -- envia dados pela serial	  
--
--    txd       -- dados vindos da serial
--    tx_data   -- barramento que contem o byte que vem do pc
--    tx_av     -- indica que existe um dado disponivel no tx_data
-- 
--          +------------------+
--          | SERIAL           |                   
--          |    +--------+    |
--     TXD  |    |        |    |
--     --------->|        |=========> tx_data (8bits)
--          |    | TRANS. |    |
--          |    |        |---------> tx_av
--          |    |        |    |
--          |    +--------+    |
--          |                  |
--          |    +--------+    |
--          |    |        |    |
--     RXD  |    |        |<========== rx_data (8bits)
--     <---------| RECEP. |<---------- rx_start
--          |    |        |----------> rx_busy
--          |    |        |    |
--          |    +--------+    |
--          +------------------+
--
--  Revisado por Fernando Moraes em 20/maio/2002
--
--#############################################################################

--*******************************************************************   
--   módulo serial
--*******************************************************************   
library IEEE;
use IEEE.std_logic_1164.all;
use IEEE.std_logic_unsigned.all;

entity serialinterface is
port(
    clock: in std_logic;                        
    reset: in std_logic;                        
    rx_data: in std_logic_vector (7 downto 0);  
    rx_start: in std_logic;                     
    rx_busy: out std_logic;                     
    rxd: out std_logic;                         
    txd: in std_logic;                          
    tx_data: out std_logic_vector (7 downto 0); 
    tx_av: out std_logic                        
  );
end serialinterface;
 
architecture serialinterface of serialinterface is
 -- auto baud
 type Sreg0_type is (S1, S2, S3, S4, S6);
 signal Sreg0:     Sreg0_type;
 signal ctr0    :  STD_LOGIC_VECTOR(16 downto 0);
 signal Bit_Cnt:   STD_LOGIC_VECTOR (1 downto 0);    
 -- geracao do clock da serial    
 signal CK_CYCLES  : STD_LOGIC_VECTOR(13 downto 0);      
 signal contador   : STD_LOGIC_VECTOR(13 downto 0);
 signal serial_clk : STD_LOGIC; 
 -- recepção
 signal word, busy : STD_LOGIC_VECTOR (8 downto 0);
 signal go         : STD_LOGIC;
 -- transmissão
 signal regin     : STD_LOGIC_VECTOR(9 downto 0);   -- 10 bits:  start/byte/stop
 signal resync, r : STD_LOGIC;
begin                                                                            
    
  --#############################################################################
  -- autobaud: o PC envia 55H (0101 0101). Este processo conta quantos 
  -- pulsos de clock 'cabem' em cada '1'. Logo, conta-se 4 vezes. Para
  -- se obter o semi-perído, divide-se a contagem por 8 (oito)
  --#############################################################################
  Sreg0_machine: process (reset, clock)
  begin
    if Reset = '0' then
      Sreg0 <= S1;
      Bit_Cnt <= "00";  
      ck_cycles <= (OTHERS=>'0');   
      ctr0 <=(OTHERS=>'0');     
    elsif clock'event and clock = '1' then
     case Sreg0 is
       when S1 => if txd = '0'  then    -- a primeira descida dos 
                       Sreg0 <= S2;     -- dados dispara o autobaud
                       ctr0 <= (OTHERS=>'0');
                  end if;
       when S2 => ctr0 <= ctr0 + 1;        -- CONTA O NÚMERO DE  PULSOS QUANDO
                  if txd = '1' then        -- O QUE VEM DA SERIAL É '1'
                       Sreg0 <= S3;
                       Bit_cnt <= Bit_Cnt + '1';
                  end if;
       when S3 => if Bit_cnt /= "00"   and txd = '0' then
                       Sreg0 <= S2;
                  elsif Bit_cnt = "00" and txd = '0' then
                       Sreg0 <= S4;
                  end if;
       when S4 => if txd = '1' then
                       Sreg0 <= S6;
                  end if;
       when S6 => Sreg0 <= S6;                     -- ARMAZENA O NUMERO DE 
                  ck_cycles <= ctr0(16 downto 3);  -- CICLOS DIVIDIDO POR 8
     end case;
   end if;
  end process; 
    
   --#############################################################################
   -- geracao do clock para a serial. de tempos em tempos ele e' resincronizado
   -- para ajuste da recepcao dos dados procenientes do PC  
   --#############################################################################
   process(resync, clock)      
    begin 
      if resync='1' then   
              contador <= (0=>'1', others=>'0');
              serial_clk <='0';       
      elsif clock'event and clock='0' then     
        if contador = ck_cycles then
              serial_clk <= not serial_clk;      
              contador <= (0=>'1', others=>'0');
        else
              contador <= contador + 1;
        end if;
    end if;       
   end process;                              
                                 
   --#############################################################################
   -- ENVIO DOS DADOS: DESTINO hardware do usuário, sinais tx_data (byte) e tx_av
   --#############################################################################

   -- registrador de deslocamento que lê o dado vindo da serial. TODOS OS
   -- bits em um no momento da detecção do resync (start bit)
   process (resync, serial_clk)
   begin
     if  resync = '1' then 
       regin <= (others=>'1');  
     elsif serial_clk'event and serial_clk='1' then
       regin <= txd & regin(9 downto 1);
     end if;
   end process;
  
   -- *****  detecta o start bit, gerando o sinal de resincronismo ******* --
   process (clock, ck_cycles)
   begin
     if ck_cycles=0 then 
          r      <= '0';
          resync <= '1';
          tx_data <= (others=>'0'); 
          tx_av <= '0';
     elsif clock'event and clock='1' then
        if r='0' and txd='0' then  --- start bit
          r      <= '1';
          resync <= '1';    
          tx_av <= '0';
        elsif r='1' and regin(0)='0' then  --- start bit chegou no último bit
          r      <= '0';
          tx_data <= regin(8 downto 1); 
          tx_av <= '1';
        else
          resync <= '0'; 
          tx_av <= '0';
        end if;
     end if;
   end process;
  
   --#############################################################################
   -- PARTE RELATIVA À RECEPÇÃO DOS DADOS: DESTINO PC, sinais rxd e rxd_busy
   --#############################################################################
 
   -- registrador de desolocamento que fica colocando sempre '1' na linha de dados
   -- quando o usuário requer dados (pulso em rx_start) é colocado o start bit e o
   -- byte a ser transmitido
   process(rx_start, reset, serial_clk)
     begin     
       if rx_start='1' or reset='0' then   
           go      <= rx_start ;
           rx_busy <= rx_start ;
           word    <= (others=>'1');
		 if reset='0' then
           	busy    <= (others=>'0');
           else
           	busy    <= (others=>'1');
           end if;
       elsif serial_clk'event and serial_clk ='1' then
           go <= '0';  -- desce o go um ciclo depois
           if go='1' then      
             word <= rx_data & '0';   -- armazena o byte que é enviado à serial 
             busy <= (8=>'0', others=>'1');
           else
             word <= '1' & word(8 downto 1); 
             busy <= '0' & busy(8 downto 1); 
             rx_busy <= busy(0);
           end if;
      end if; 
    end process;    
    
    rxd <= word(0);   -- bit de saida, que vai para o PC
   
end serialinterface;
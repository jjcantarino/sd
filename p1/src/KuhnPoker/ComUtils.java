package KuhnPoker;
import java.net.*;
import java.io.*;
import java.util.Locale;

public class ComUtils
{
  /* Mida d'una cadena de caracters */
  private final int STRSIZE = 40;
  /* Objectes per escriure i llegir dades */
  private DataInputStream dis;
  private DataOutputStream dos;

  public ComUtils(Socket socket) throws IOException
  {
    dis = new DataInputStream(socket.getInputStream());
    dos = new DataOutputStream(socket.getOutputStream());
  }
  
  public ComUtils(File file) throws IOException{
    dis = new DataInputStream(new FileInputStream(file));
    dos = new DataOutputStream(new FileOutputStream(file));
  }

    /* Llegir un enter de 32 bits */
  public int read_int32() throws IOException
  {
    byte bytes[] = new byte[4];
    bytes  = read_bytes(4);

    return bytesToInt32(bytes,"be");
  }

  /* Escriure un enter de 32 bits */
  public void write_int32(int number) throws IOException
  {
    byte bytes[]=new byte[4];

    int32ToBytes(number,bytes,"be");
    dos.write(bytes, 0, 4);
  }

  /* Llegir un string de mida STRSIZE */
  public String read_string() throws IOException
  {
    String str;
    byte bStr[] = new byte[STRSIZE];
    char cStr[] = new char[STRSIZE];

    bStr = read_bytes(STRSIZE);

    for(int i = 0; i < STRSIZE;i++)
      cStr[i]= (char) bStr[i];

    str = String.valueOf(cStr);

    return str.trim();
  }

  /* Escriure un string */
  public void write_string(String str) throws IOException
  { 
    int numBytes, lenStr; 
    byte bStr[] = new byte[STRSIZE];

    lenStr = str.length();

    if (lenStr > STRSIZE)
      numBytes = STRSIZE;
    else
      numBytes = lenStr;

    for(int i = 0; i < numBytes; i++)
      bStr[i] = (byte) str.charAt(i);

    for(int i = numBytes; i < STRSIZE; i++)
      bStr[i] = (byte) ' ';

    dos.write(bStr, 0,STRSIZE);
  }

  /* Passar d'enters a bytes */
  private int int32ToBytes(int number,byte bytes[], String endianess)
  {
    if("be".equals(endianess.toLowerCase()))
    {
      bytes[0] = (byte)((number >> 24) & 0xFF);
      bytes[1] = (byte)((number >> 16) & 0xFF);
      bytes[2] = (byte)((number >> 8) & 0xFF);
      bytes[3] = (byte)(number & 0xFF);
    }
    else
    {
      bytes[0] = (byte)(number & 0xFF);
      bytes[1] = (byte)((number >> 8) & 0xFF);
      bytes[2] = (byte)((number >> 16) & 0xFF);
      bytes[3] = (byte)((number >> 24) & 0xFF);
    }
    return 4;
  }

  /* Passar de bytes a enters */
  private int bytesToInt32(byte bytes[], String endianess)
  {
    int number;

    if("be".equals(endianess.toLowerCase()))
    {
      number=((bytes[0] & 0xFF) << 24) | ((bytes[1] & 0xFF) << 16) |
        ((bytes[2] & 0xFF) << 8) | (bytes[3] & 0xFF);
    }
    else
    {
      number=(bytes[0] & 0xFF) | ((bytes[1] & 0xFF) << 8) |
        ((bytes[2] & 0xFF) << 16) | ((bytes[3] & 0xFF) << 24);
    }
    return number;
  }
	//llegir bytes.
  private byte[] read_bytes(int numBytes) throws IOException{
    int len=0 ;
    byte bStr[] = new byte[numBytes];
    int bytesread=0; 
    do {
      bytesread= dis.read(bStr, len, numBytes-len);
      if (bytesread == -1)
        throw new IOException("Broken Pipe");
      len += bytesread;
     } while (len < numBytes);
    return bStr;
  }
	
	/* Llegir un string  mida variable size = nombre de bytes especifica la longitud*/
	public  String read_string_variable(int size) throws IOException
	{
		byte bHeader[]=new byte[size];
		char cHeader[]=new char[size];
		int numBytes=0;
		
		// Llegim els bytes que indiquen la mida de l'string
		bHeader = read_bytes(size);
		// La mida de l'string ve en format text, per tant creem un string i el parsejem
		for(int i=0;i<size;i++){
			cHeader[i]=(char)bHeader[i]; }
		numBytes=Integer.parseInt(new String(cHeader));
		
		// Llegim l'string
		byte bStr[]=new byte[numBytes];
		char cStr[]=new char[numBytes];
		bStr = read_bytes(numBytes);
		for(int i=0;i<numBytes;i++)
			cStr[i]=(char)bStr[i];
		return String.valueOf(cStr);
	}
	
	/* Escriure un string mida variable, size = nombre de bytes especifica la longitud  */
	/* String str = string a escriure.*/
	public  void write_string_variable(int size,String str) throws IOException
	{
		
		// Creem una seqüència amb la mida
		byte bHeader[]=new byte[size];
		String strHeader;
		int numBytes=0; 
		
		// Creem la capçalera amb el nombre de bytes que codifiquen la mida
		numBytes=str.length();
		
		strHeader=String.valueOf(numBytes);
	    int len;
		if ((len=strHeader.length()) < size)
	    	for (int i =len; i< size;i++){
	    		strHeader= "0"+strHeader;}
		for(int i=0;i<size;i++)
			bHeader[i]=(byte)strHeader.charAt(i);
		// Enviem la capçalera
		dos.write(bHeader, 0, size);
		// Enviem l'string writeBytes de DataOutputStrem no envia el byte més alt dels chars.
		dos.writeBytes(str);
	}
    public void writeTest() throws IOException{
        
        write_string_variable(20,"Alexander Bevzenko");
        write_int32(127);
        write_string("pshhhhh, yoba eto ti?");
        writeChar((char)99);
        
    }
    public String readTest() throws IOException{
        
        return read_string_variable(20)+" \n"+ String.valueOf(read_int32()) +" \n"+ read_string()+ " \n"+readChar()  ;
        
    }
    public void writeChar (char c) throws IOException{
        
        dos.writeChar(c);
        
        
    }
    public char readChar() throws IOException{
        
        return dis.readChar();
        
    }
    /**
     read protocol message
     * reads next message in input stream, if it doesnt belong to protocol throws exception 
     */
    public String read_PtMsg() throws IOException
  {
    String str;
    byte baseMsg[] = new byte[3];
    //char cStr[] = new char[4];
    
    str= new String(read_bytes(1)).toUpperCase();
    //we will ignore all input (like space) until we get character that belongs to protocol message 
    while(evalFirstSymbol(str)){
        str= new String(read_bytes(1)).toUpperCase();
    }
    //then we will read all message
    baseMsg = read_bytes(3);
    /*
    for(int i = 0; i < 4;i++)
      cStr[i]= (char) baseMsg[i];*/

    str += new String(baseMsg).toUpperCase();
    int moreChars = protocolCheck(str); 
    if (moreChars!=0){
        str+= new String(read_bytes(moreChars));// add error thaction
    }

    return str.trim();
  }

  /**
   * writes given string to output stream  as ASCII characters
     * @param str string to send
   */
  public void write_PtMsg(String str) throws IOException
  { 
    int numBytes; 
    byte bStr[] = stringToASCIIConvert(str);

    numBytes = bStr.length;



    dos.write(bStr, 0,numBytes);
  }
  /**
   *Converts given string into ascii-compatible byte array
   * 
  */
  private static byte[] stringToASCIIConvert(String input) {
      byte[] bytes = new byte[input.length()];
      for (int i = 0; i < bytes.length; i++) {
          bytes[i] = (byte) input.charAt(i);
      }
      return bytes;
  }
  /**
   checks if recieved message belongs to The Protocol and indicates additional number of bytes to read if message needs it
   */
  private int protocolCheck(String input){
      switch (input){
            case "STRT":
                return 4;
            case "BLNC":
                return 4;
            case "SHOW":
                return 2;
            case "WINN":
                return 2;
            case "DEAL":
                return 2;
            case "EXIT":
                return 0;   
            case "BETT":
                return 0;
            case "CHCK":
                return 0;
            case "CALL":
                return 0;
            case "FOLD":
                return 0;
            case "TURN":
                return 2;
            case "RPLY":
                return 0;
            case "ERRO":
                return 4;
            default:
                return 0;//we recieved something strange
                //throw new IOException("Invalid message"); //we recieved something strange
                
        }
    
  }
  /**
   * indicates if first symbol belongs to protocol message
   */
    private boolean evalFirstSymbol(String input){
        switch(input){
            case "S":
            case "B":
            case "W":
            case "D":
            case "E":
            case "C":                
            case "F":
            case "T":
            case "R":
                return false;
            default:
                return true;
      }
      
    }
}


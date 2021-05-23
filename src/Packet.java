import java.io.IOException;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class Packet implements Serializable{
    private int tipo;
    private String id;
    private byte [] data;
    private int frag; //0 false, 1 true
    private int checksum;
    private int offset;

    public Packet(int tipo, int porta_origem, String ip, int frag, byte [] dados, int checksum, int offset){
        this.tipo = tipo;
        this.id = ip + "-" + porta_origem;
        this.frag = frag;
        this.data = dados;
        this.checksum = checksum;
        this.offset = offset;
    }

    Packet(byte[] array) throws UnknownHostException{
		String key;

		byte [] b = new byte[4];
		System.arraycopy(array,8,b,0,4);
		key = InetAddress.getByAddress(b).getHostAddress();

		key += "-" + ByteBuffer.wrap(array,4,4).getInt();

		this.id = key;
		this.tipo = ByteBuffer.wrap(array,0,4).getInt();
		this.offset = ByteBuffer.wrap(array,12,4).getInt();
        this.frag = ByteBuffer.wrap(array,16,4).getInt();
        this.checksum = ByteBuffer.wrap(array,20,4).getInt();

		byte [] data = new byte[array.length-24];
		System.arraycopy(array,24,data,0,array.length-24);
		this.data = data;
	}


    public byte[] toBytes() throws IOException{


		String [] aux = this.id.split("-");
		//byte[] address = InetAddress.getByName(aux[0]).getAddress(); //4
		//byte[] n_transferencia = intToBytes(Integer.parseInt(aux[1])); //4
		//byte[] porta = intToBytes(Integer.parseInt(aux[2])); //4
		//byte[] chunk = intToBytes(this.chunk); //4
		byte[] tipo_buffer = intToBytes(this.tipo); //4
        byte[] porta_origem_buffer = intToBytes(Integer.parseInt(aux[1]));
		byte[] ip_origem_buffer = InetAddress.getByName(aux[0]).getAddress();
        byte[] frag_buffer = intToBytes(this.frag);
        byte[] checksum_buffer = intToBytes(this.checksum);
        byte[] offset_buffer = intToBytes(this.offset);
		byte [] buffer = new byte[4*6 + this.data.length];


		System.arraycopy(tipo_buffer,0,buffer,0,4);
        System.arraycopy(porta_origem_buffer,0,buffer,4,4);
		System.arraycopy(ip_origem_buffer,0,buffer,8,4);
		System.arraycopy(offset_buffer,0,buffer,12,4);
		System.arraycopy(frag_buffer,0,buffer,16,4);
		System.arraycopy(checksum_buffer,0,buffer,20 ,4);
		System.arraycopy(data,0,buffer,24 ,data.length);

		return buffer;
	}

    private byte[] intToBytes( final int x ) {
		ByteBuffer bb = ByteBuffer.allocate(4);
		bb.putInt(x);
		return bb.array();
	}

	int getTipo(){
		return this.tipo;
	}
	
    int getPorta(){
		String [] aux = this.id.split("-");
        return (Integer.parseInt(aux[1]));
    }

	String getIP(){
		String [] aux = this.id.split("-");
        return aux[0];
	}
    
    public String toString(){
		StringBuilder s = new StringBuilder();

		s.append("ID: ");
		s.append(this.id + "\n");
		Charset charset = StandardCharsets.US_ASCII;
		String string = charset.decode(ByteBuffer.wrap(data))
				.toString();
		s.append(string);
		return s.toString();
	}

}

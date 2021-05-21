import java.io.IOException;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class Packet implements Serializable{
    private int tipo;
    private int id;
    private int porta_origem;
    private byte [] data;
    private int frag; //0 false, 1 true
    private int checksum;
    private int offset;
    private String ficheiro;

    public Packet(int tipo, int id, int porta_origem, int frag, byte [] dados, int checksum, int offset, String ficheiro){
        this.tipo = tipo;
        this.id = id;
        this.frag = frag;
        this.data = dados;
        this.checksum = checksum;
        this.ficheiro = ficheiro;
        this.offset = offset;
        this.porta_origem = porta_origem;
    }

    Packet(byte[] array) throws UnknownHostException{
		//String key;

		//byte [] b = new byte[4];
		//System.arraycopy(array,0,b,0,4);
		//key = InetAddress.getByAddress(b).getHostAddress();

		//key += "-" + ByteBuffer.wrap(array,4,4).getInt();
		//key += "-" + ByteBuffer.wrap(array,8,4).getInt();

		//this.transfer_id = key;
		this.id = ByteBuffer.wrap(array,0,4).getInt();
		this.tipo = ByteBuffer.wrap(array,4,4).getInt();
        this.porta_origem = ByteBuffer.wrap(array,8,4).getInt();
		this.offset = ByteBuffer.wrap(array,12,4).getInt();
        this.frag = ByteBuffer.wrap(array,16,4).getInt();
        this.ficheiro = "" + ByteBuffer.wrap(array,20,4).getInt();
        this.checksum = ByteBuffer.wrap(array,24,4).getInt();

		byte [] data = new byte[array.length-28];
		System.arraycopy(array,24,data,0,array.length-28);
		this.data = data;
	}


    public byte[] toBytes() throws IOException{

		//String [] aux = this.id.split("-");
		//byte[] address = InetAddress.getByName(aux[0]).getAddress(); //4
		//byte[] n_transferencia = intToBytes(Integer.parseInt(aux[1])); //4
		//byte[] porta = intToBytes(Integer.parseInt(aux[2])); //4
		//byte[] chunk = intToBytes(this.chunk); //4
        byte[] id_buffer = intToBytes(this.id); //4
		byte[] tipo_buffer = intToBytes(this.tipo); //4
        byte[] porta_origem_buffer = intToBytes(this.porta_origem);
        byte[] frag_buffer = intToBytes(this.frag);
        byte[] checksum_buffer = intToBytes(this.checksum);
        byte[] offset_buffer = intToBytes(this.offset);
        byte[] ficheiro_buffer = intToBytes(Integer.parseInt(this.ficheiro));
		byte [] buffer = new byte[4*7 + this.data.length];


		System.arraycopy(id_buffer,0,buffer,0,4);
		System.arraycopy(tipo_buffer,0,buffer,4,4);
        System.arraycopy(porta_origem_buffer,0,buffer,8,4);
		System.arraycopy(offset_buffer,0,buffer,12,4);
		System.arraycopy(frag_buffer,0,buffer,16,4);
		System.arraycopy(ficheiro_buffer,0,buffer,20,4);
		System.arraycopy(checksum_buffer,0,buffer,24,4);
		System.arraycopy(data,0,buffer,28,data.length);

		return buffer;
	}

    private byte[] intToBytes( final int x ) {
		ByteBuffer bb = ByteBuffer.allocate(4);
		bb.putInt(x);
		return bb.array();
	}

    int getPorta(){
        return this.porta_origem;
    }
    
    public String toString(){
		StringBuilder s = new StringBuilder();

		s.append("ID: ");
		s.append(this.id);
		return s.toString();
	}

}

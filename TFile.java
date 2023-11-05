import java.util.Map;
import java.util.HashMap;

public class TFile
{
    public static final long MAXB = 2200;
    public static final long MINB = 1200;
    public static final long MULTB = 16384; // 16 KiB

    private String hash; // a usar MD5 hashing (128-bit)
    private String filename;
    private String tipo;
    private String desc;
    private long byteS;
    private long blockS;
    private Map<Integer, Long> blockM; // num bloco -> offset

    public TFile(String hash, String filename, String tipo, String desc, long byteS) {
	    this.hash = hash;
        this.filename = filename;
	    this.tipo = tipo;
        this.desc = desc;
        this.byteS = byteS;
        this.blockS = calcBlockS();
        this.blockM = new HashMap<Integer, Long>();
        genBlockMap();
    }

    /*
      sets e gets
     */

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public long getByteS() {
        return byteS;
    }

    public void setByteS(long byteS) {
        this.byteS = byteS;
    }

    public long getBlockS() {
        return blockS;
    }

    public void setBlockS(long blockS) {
        this.blockS = blockS;
    }

    public Map<Integer, Long> getBlockM() {
	    Map<Integer, Long> res = new HashMap<>();
	    res.putAll(this.blockM);
	    return res;
    }

    public void setBlockM(Map<Integer, Long> m) {
	    this.blockM.putAll(m);
    }

    /*
      métodos
     */

    public long calcBlockS() {
        long bytes = this.byteS;
        long blockS = MULTB;
        long blockNum = bytes / blockS;
        while(blockNum > MAXB)
        {
            blockS *= 2;
            blockNum = bytes / blockS;
        }
        return (blockS);
    }

    public void genBlockMap() {
        long r = 1;
        if (this.byteS % this.blockS == 0) r = 0;
        long blockNum = this.byteS / this.blockS + r;
        long offset = 0;

        for(int i = 1; i <= blockNum; i++) {
            this.blockM.put(i, offset);
            offset += this.blockS;
        }
    }

    public long getBlockNum()
    {
	    return this.blockM.size();
    }
}

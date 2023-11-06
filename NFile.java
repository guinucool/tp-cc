import java.io.File;
import java.nio.file.Files;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class NFile
{   
    public static final String NHASH = "d41d8cd98f00b204e9800998ecf8427e";

    private String hash; // a usar MD5 hashing (128-bit)
    private String filename;
    private String tipo;
    private String desc;
    private long byteS;

    public NFile(String path) {
        File f = new File(path);
        String hash;

        try {
            this.hash = md5checksum(f);
        }
        catch (Exception e) {
            this.hash = NHASH;
        }

        this.filename = f.getName();
        String ext;

        try {
            ext = Files.probeContentType(f.toPath());
        }
        catch (Exception e) {
            ext = "";
        }

        this.tipo = ext;
        this.desc = "";
        this.byteS = f.length();
    }

    public String md5checksum(File file) throws IOException, NoSuchAlgorithmException {
	    MessageDigest digest = MessageDigest.getInstance("MD5");
        FileInputStream fis = new FileInputStream(file);
        byte[] byteArray = new byte[1024];
        int bytesCount = 0;

        while ((bytesCount = fis.read(byteArray)) != -1)
        {
            digest.update(byteArray, 0, bytesCount);
        }
 
        fis.close();

        byte[] bytes = digest.digest();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < bytes.length; i++) {
	    sb.append(Integer
		      .toString((bytes[i] & 0xff) + 0x100, 16)
		      .substring(1));
        }
        
        return sb.toString();
    }

    public static void main(String[] args) {
        
        File path = new File(args[0]);
        File fileList[] = path.listFiles();

        NFile nf = new NFile(fileList[0].getAbsolutePath()); 

        System.out.println(nf.hash);
    }
}

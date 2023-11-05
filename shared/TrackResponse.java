package shared;

public class TrackResponse extends TrackMessage {
    
    public enum Code {
        LURV, /* Update request após login */
        URSU, /* Update request registado com sucesso */
        LBFS, /* Localização dos ficheiros e dos seus blocos */
        QUIT, /* Pedido de saída bem sucedido */
        INVA, /* Argumento inválido */
        INVN, /* Node inválido */
        FAIL  /* Algo de errado ocorreu */
    }
    
    private Code code;

    public TrackResponse(Code code) {
        super();
        this.code = code;
    }

    public TrackResponse(Code code, String arg) {
        super(arg);
        this.code = code;
    }

    public Code getCode() {
        return this.code;
    }
}


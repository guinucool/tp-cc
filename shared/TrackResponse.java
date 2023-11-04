package shared;

public class TrackResponse {
    
    public enum Code {
        FAIL,
        SUC    
    }
    
    private Code code;
    private String args;

    public TrackResponse(Code code, String args) {
        this.code = code;
        this.args = args;
    }

    public Code getCode() {
        return this.code;
    }

    public String getArguments() {
        return this.args;
    }  
    
    public Code setCode(Code code) {
        return this.code = code;
    }

    public String setArguments(String args) {
        return this.args = args;
    }   
}


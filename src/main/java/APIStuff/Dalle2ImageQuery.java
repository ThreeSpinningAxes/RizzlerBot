package APIStuff;

public class Dalle2ImageQuery {

    private String prompt;

    private int n;

    private String size;


    public String getPrompt() {
        return prompt;
    }

    public int getN() {
        return n;
    }


    public String getSize() {
        return size;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }
    public void setN(int n) {
        this.n = n;
    }

    public void setSize(String size) {
        this.size = size;
    }
}

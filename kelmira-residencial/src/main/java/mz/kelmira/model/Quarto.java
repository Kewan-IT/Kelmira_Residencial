package mz.kelmira.model;

public class Quarto {
    private int id;
    private String numero;
    private String tipo;
    private double preco;
    private String status;

    public Quarto() {}

    public Quarto(int id, String numero, String tipo, double preco, String status) {
        this.id = id;
        this.numero = numero;
        this.tipo = tipo;
        this.preco = preco;
        this.status = status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public double getPreco() { return preco; }
    public void setPreco(double preco) { this.preco = preco; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

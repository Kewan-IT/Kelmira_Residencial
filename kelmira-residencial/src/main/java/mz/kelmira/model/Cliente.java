package mz.kelmira.model;

public class Cliente {
    private int id;
    private String tipo; // singular ou institucional
    private String nome;
    private String bi;
    private String nuit;
    private String representante;
    private String telefone;
    private String email;
    private String endereco;

    public Cliente() {}

    public Cliente(int id, String tipo, String nome, String bi, String nuit,
                    String representante, String telefone, String email, String endereco) {
        this.id = id;
        this.tipo = tipo;
        this.nome = nome;
        this.bi = bi;
        this.nuit = nuit;
        this.representante = representante;
        this.telefone = telefone;
        this.email = email;
        this.endereco = endereco;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getBi() { return bi; }
    public void setBi(String bi) { this.bi = bi; }

    public String getNuit() { return nuit; }
    public void setNuit(String nuit) { this.nuit = nuit; }

    public String getRepresentante() { return representante; }
    public void setRepresentante(String representante) { this.representante = representante; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }
}

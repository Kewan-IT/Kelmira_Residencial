package mz.kelmira.model;

import java.sql.Date;

public class Reserva {
    private int id;
    private int clienteId;
    private int quartoId;
    private Date dataEntrada;
    private Date dataSaida;
    private double valorTotal;
    private String status;

    // Campos extras para exibição (join)
    private String clienteNome;
    private String quartoNumero;
    private double quartoPreco;

    public Reserva() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getClienteId() { return clienteId; }
    public void setClienteId(int clienteId) { this.clienteId = clienteId; }

    public int getQuartoId() { return quartoId; }
    public void setQuartoId(int quartoId) { this.quartoId = quartoId; }

    public Date getDataEntrada() { return dataEntrada; }
    public void setDataEntrada(Date dataEntrada) { this.dataEntrada = dataEntrada; }

    public Date getDataSaida() { return dataSaida; }
    public void setDataSaida(Date dataSaida) { this.dataSaida = dataSaida; }

    public double getValorTotal() { return valorTotal; }
    public void setValorTotal(double valorTotal) { this.valorTotal = valorTotal; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getClienteNome() { return clienteNome; }
    public void setClienteNome(String clienteNome) { this.clienteNome = clienteNome; }

    public String getQuartoNumero() { return quartoNumero; }
    public void setQuartoNumero(String quartoNumero) { this.quartoNumero = quartoNumero; }

    public double getQuartoPreco() { return quartoPreco; }
    public void setQuartoPreco(double quartoPreco) { this.quartoPreco = quartoPreco; }
}

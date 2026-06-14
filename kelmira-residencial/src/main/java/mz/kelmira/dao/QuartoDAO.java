package mz.kelmira.dao;

import mz.kelmira.model.Quarto;
import mz.kelmira.util.ConexaoDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QuartoDAO {

    public List<Quarto> listarTodos() throws SQLException {
        List<Quarto> lista = new ArrayList<>();
        String sql = "SELECT * FROM quartos ORDER BY numero";

        try (Connection conn = ConexaoDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lista.add(new Quarto(
                    rs.getInt("id"),
                    rs.getString("numero"),
                    rs.getString("tipo"),
                    rs.getDouble("preco"),
                    rs.getString("status"),
                    rs.getString("imagem")
                ));
            }
        }
        return lista;
    }

    public Quarto buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM quartos WHERE id = ?";

        try (Connection conn = ConexaoDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Quarto(
                    rs.getInt("id"),
                    rs.getString("numero"),
                    rs.getString("tipo"),
                    rs.getDouble("preco"),
                    rs.getString("status"),
                    rs.getString("imagem")
                );
            }
        }
        return null;
    }

    public void inserir(Quarto q) throws SQLException {
        String sql = "INSERT INTO quartos (numero, tipo, preco, status, imagem) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = ConexaoDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, q.getNumero());
            stmt.setString(2, q.getTipo());
            stmt.setDouble(3, q.getPreco());
            stmt.setString(4, q.getStatus());
            stmt.setString(5, q.getImagem());
            stmt.executeUpdate();
        }
    }

    public void atualizar(Quarto q) throws SQLException {
        String sql = "UPDATE quartos SET numero=?, tipo=?, preco=?, status=?, imagem=? WHERE id=?";

        try (Connection conn = ConexaoDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, q.getNumero());
            stmt.setString(2, q.getTipo());
            stmt.setDouble(3, q.getPreco());
            stmt.setString(4, q.getStatus());
            stmt.setString(5, q.getImagem());
            stmt.setInt(6, q.getId());
            stmt.executeUpdate();
        }
    }

    public void remover(int id) throws SQLException {
        String sql = "DELETE FROM quartos WHERE id = ?";

        try (Connection conn = ConexaoDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }
}

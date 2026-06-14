package mz.kelmira.dao;

import mz.kelmira.model.Cliente;
import mz.kelmira.util.ConexaoDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClienteDAO {

    private Cliente mapear(ResultSet rs) throws SQLException {
        return new Cliente(
            rs.getInt("id"),
            rs.getString("tipo"),
            rs.getString("nome"),
            rs.getString("bi"),
            rs.getString("nuit"),
            rs.getString("representante"),
            rs.getString("telefone"),
            rs.getString("email"),
            rs.getString("endereco")
        );
    }

    public List<Cliente> listarTodos() throws SQLException {
        List<Cliente> lista = new ArrayList<>();
        String sql = "SELECT * FROM clientes ORDER BY nome";

        try (Connection conn = ConexaoDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public Cliente buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM clientes WHERE id = ?";

        try (Connection conn = ConexaoDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapear(rs);
            }
        }
        return null;
    }

    public void inserir(Cliente c) throws SQLException {
        String sql = "INSERT INTO clientes (tipo, nome, bi, nuit, representante, telefone, email, endereco) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConexaoDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, c.getTipo());
            stmt.setString(2, c.getNome());
            stmt.setString(3, c.getBi());
            stmt.setString(4, c.getNuit());
            stmt.setString(5, c.getRepresentante());
            stmt.setString(6, c.getTelefone());
            stmt.setString(7, c.getEmail());
            stmt.setString(8, c.getEndereco());
            stmt.executeUpdate();
        }
    }

    public void atualizar(Cliente c) throws SQLException {
        String sql = "UPDATE clientes SET tipo=?, nome=?, bi=?, nuit=?, representante=?, telefone=?, email=?, endereco=? WHERE id=?";

        try (Connection conn = ConexaoDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, c.getTipo());
            stmt.setString(2, c.getNome());
            stmt.setString(3, c.getBi());
            stmt.setString(4, c.getNuit());
            stmt.setString(5, c.getRepresentante());
            stmt.setString(6, c.getTelefone());
            stmt.setString(7, c.getEmail());
            stmt.setString(8, c.getEndereco());
            stmt.setInt(9, c.getId());
            stmt.executeUpdate();
        }
    }

    public void remover(int id) throws SQLException {
        String sql = "DELETE FROM clientes WHERE id = ?";

        try (Connection conn = ConexaoDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }
}

package mz.kelmira.dao;

import mz.kelmira.model.Reserva;
import mz.kelmira.util.ConexaoDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReservaDAO {

    public List<Reserva> listarTodas() throws SQLException {
        List<Reserva> lista = new ArrayList<>();
        String sql = "SELECT r.*, c.nome AS cliente_nome, q.numero AS quarto_numero " +
                     "FROM reservas r " +
                     "JOIN clientes c ON r.cliente_id = c.id " +
                     "JOIN quartos q ON r.quarto_id = q.id " +
                     "ORDER BY r.data_entrada DESC";

        try (Connection conn = ConexaoDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Reserva r = new Reserva();
                r.setId(rs.getInt("id"));
                r.setClienteId(rs.getInt("cliente_id"));
                r.setQuartoId(rs.getInt("quarto_id"));
                r.setDataEntrada(rs.getDate("data_entrada"));
                r.setDataSaida(rs.getDate("data_saida"));
                r.setValorTotal(rs.getDouble("valor_total"));
                r.setStatus(rs.getString("status"));
                r.setClienteNome(rs.getString("cliente_nome"));
                r.setQuartoNumero(rs.getString("quarto_numero"));
                lista.add(r);
            }
        }
        return lista;
    }

    public void inserir(Reserva r) throws SQLException {
        String sql = "INSERT INTO reservas (cliente_id, quarto_id, data_entrada, data_saida, valor_total, status) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConexaoDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, r.getClienteId());
            stmt.setInt(2, r.getQuartoId());
            stmt.setDate(3, r.getDataEntrada());
            stmt.setDate(4, r.getDataSaida());
            stmt.setDouble(5, r.getValorTotal());
            stmt.setString(6, r.getStatus());
            stmt.executeUpdate();
        }
    }

    public void atualizarStatus(int id, String status) throws SQLException {
        String sql = "UPDATE reservas SET status = ? WHERE id = ?";

        try (Connection conn = ConexaoDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            stmt.setInt(2, id);
            stmt.executeUpdate();
        }
    }

    public Reserva buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM reservas WHERE id = ?";

        try (Connection conn = ConexaoDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Reserva r = new Reserva();
                r.setId(rs.getInt("id"));
                r.setClienteId(rs.getInt("cliente_id"));
                r.setQuartoId(rs.getInt("quarto_id"));
                r.setDataEntrada(rs.getDate("data_entrada"));
                r.setDataSaida(rs.getDate("data_saida"));
                r.setValorTotal(rs.getDouble("valor_total"));
                r.setStatus(rs.getString("status"));
                return r;
            }
        }
        return null;
    }

    public boolean existeConflito(int quartoId, java.sql.Date entrada, java.sql.Date saida) throws SQLException {
        String sql = "SELECT COUNT(*) FROM reservas " +
                     "WHERE quarto_id = ? AND status = 'ativa' " +
                     "AND data_entrada < ? AND data_saida > ?";

        try (Connection conn = ConexaoDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, quartoId);
            stmt.setDate(2, saida);
            stmt.setDate(3, entrada);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }
}

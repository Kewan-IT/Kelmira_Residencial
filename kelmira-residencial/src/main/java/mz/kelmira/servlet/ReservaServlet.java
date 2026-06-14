package mz.kelmira.servlet;

import mz.kelmira.dao.ClienteDAO;
import mz.kelmira.dao.QuartoDAO;
import mz.kelmira.dao.ReservaDAO;
import mz.kelmira.model.Cliente;
import mz.kelmira.model.Quarto;
import mz.kelmira.model.Reserva;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class ReservaServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String acao = request.getParameter("acao");
        ReservaDAO reservaDao = new ReservaDAO();
        QuartoDAO quartoDao = new QuartoDAO();

        try {
            if ("checkout".equals(acao)) {
                int id = Integer.parseInt(request.getParameter("id"));
                Reserva reserva = reservaDao.buscarPorId(id);

                reservaDao.atualizarStatus(id, "finalizada");

                Quarto q = quartoDao.buscarPorId(reserva.getQuartoId());
                q.setStatus("disponivel");
                quartoDao.atualizar(q);

                response.setStatus(302);
                response.setHeader("Location", "/reservas");
                return;
            }

            if ("cancelar".equals(acao)) {
                int id = Integer.parseInt(request.getParameter("id"));
                Reserva reserva = reservaDao.buscarPorId(id);

                reservaDao.atualizarStatus(id, "cancelada");

                Quarto q = quartoDao.buscarPorId(reserva.getQuartoId());
                q.setStatus("disponivel");
                quartoDao.atualizar(q);

                response.setStatus(302);
                response.setHeader("Location", "/reservas");
                return;
            }

            renderizarPagina(request, response);

        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            int clienteId = Integer.parseInt(request.getParameter("cliente_id"));
            int quartoId = Integer.parseInt(request.getParameter("quarto_id"));
            String dataEntradaStr = request.getParameter("data_entrada");
            String dataSaidaStr = request.getParameter("data_saida");

            Date dataEntrada = Date.valueOf(dataEntradaStr);
            Date dataSaida = Date.valueOf(dataSaidaStr);

            QuartoDAO quartoDao = new QuartoDAO();
            ReservaDAO reservaDao = new ReservaDAO();

            Quarto quarto = quartoDao.buscarPorId(quartoId);

            long noites = ChronoUnit.DAYS.between(dataEntrada.toLocalDate(), dataSaida.toLocalDate());
            if (noites <= 0) {
                request.setAttribute("erro", "A data de saída deve ser posterior à data de entrada.");
                renderizarPagina(request, response);

            if (reservaDao.existeConflito(quartoId, dataEntrada, dataSaida)) {
                request.setAttribute("erro", "Este quarto ja esta reservado para o periodo selecionado.");
                renderizarPagina(request, response);
                return;
            }
                return;
            }

            double valorTotal = noites * quarto.getPreco();

            Reserva r = new Reserva();
            r.setClienteId(clienteId);
            r.setQuartoId(quartoId);
            r.setDataEntrada(dataEntrada);
            r.setDataSaida(dataSaida);
            r.setValorTotal(valorTotal);
            r.setStatus("ativa");

            reservaDao.inserir(r);

            // Atualizar status do quarto para ocupado
            quarto.setStatus("ocupado");
            quartoDao.atualizar(quarto);

            response.setStatus(302);
            response.setHeader("Location", "/reservas");

        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    private void renderizarPagina(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = response.getWriter();

        try {
            ClienteDAO clienteDao = new ClienteDAO();
            QuartoDAO quartoDao = new QuartoDAO();
            ReservaDAO reservaDao = new ReservaDAO();

            List<Cliente> clientes = clienteDao.listarTodos();
            List<Quarto> quartos = quartoDao.listarTodos();
            List<Reserva> reservas = reservaDao.listarTodas();

            out.println("<!DOCTYPE html>");
            out.println("<html lang='pt'>");
            out.println("<head><meta charset='UTF-8'><title>Reservas - Kelmira Residencial</title>");
            out.println("<link rel='stylesheet' href='css/dashboard.css'>");
            out.println("<link rel='stylesheet' href='css/quartos.css'></head>");
            out.println("<body>");

            out.println("<div class='sidebar'>");
            out.println("<h2>Kelmira Residencial</h2>");
            out.println("<nav>");
            out.println("<a href='dashboard'>🏠 Início</a>");
            out.println("<a href='quartos'>🛏️ Quartos</a>");
            out.println("<a href='clientes'>👤 Clientes</a>");
            out.println("<a href='reservas' class='active'>📅 Reservas</a>");
            out.println("<a href='logout'>🚪 Sair</a>");
            out.println("</nav>");
            out.println("</div>");

            out.println("<div class='main-content'>");
            out.println("<h1>Gestão de Reservas</h1>");

            // Formulário
            out.println("<div class='form-card'>");
            out.println("<h2>Nova Reserva</h2>");

            Object erro = request.getAttribute("erro");
            if (erro != null) {
                out.println("<p style=\"color:red; font-weight:bold;\">" + erro + "</p>");
            }

            out.println("<form action='/reservas' method='post'>");

            out.println("<label>Cliente</label>");
            out.println("<select name='cliente_id' required>");
            for (Cliente c : clientes) {
                out.println("<option value='" + c.getId() + "'>" + c.getNome() + "</option>");
            }
            out.println("</select>");

            out.println("<label>Quarto</label>");
            out.println("<select name='quarto_id' required>");
            for (Quarto q : quartos) {
                if ("disponivel".equals(q.getStatus())) {
                    out.println("<option value='" + q.getId() + "'>Quarto " + q.getNumero() +
                                 " - " + q.getTipo() + " (" + String.format("%.2f", q.getPreco()) + " MT/noite)</option>");
                }
            }
            out.println("</select>");

            out.println("<label>Data de Entrada</label>");
            out.println("<input type='date' name='data_entrada' required>");

            out.println("<label>Data de Saída</label>");
            out.println("<input type='date' name='data_saida' required>");

            out.println("<button type='submit'>Criar Reserva</button>");
            out.println("</form>");
            out.println("</div>");

            // Tabela de reservas
            out.println("<div class='table-card'>");
            out.println("<h2>Lista de Reservas</h2>");
            out.println("<table>");
            out.println("<tr><th>Cliente</th><th>Quarto</th><th>Entrada</th><th>Saída</th><th>Valor (MT)</th><th>Status</th><th>Ações</th></tr>");

            for (Reserva r : reservas) {
                out.println("<tr>");
                out.println("<td>" + r.getClienteNome() + "</td>");
                out.println("<td>" + r.getQuartoNumero() + "</td>");
                out.println("<td>" + r.getDataEntrada() + "</td>");
                out.println("<td>" + r.getDataSaida() + "</td>");
                out.println("<td>" + String.format("%.2f", r.getValorTotal()) + "</td>");
                out.println("<td><span class='status " + r.getStatus() + "'>" + r.getStatus() + "</span></td>");
                out.println("<td>");
                out.println("<a href='/comprovante?id=" + r.getId() + "' target='_blank'>PDF</a> | ");
                if ("ativa".equals(r.getStatus())) {
                    out.println("<a href='reservas?acao=checkout&id=" + r.getId() + "' onclick=\"return confirm('Confirmar check-out?')\">Check-out</a> | ");
                    out.println("<a href='reservas?acao=cancelar&id=" + r.getId() + "' onclick=\"return confirm('Cancelar reserva?')\">Cancelar</a>");
                } else {
                    out.println("-");
                }
                out.println("</td>");
                out.println("</tr>");
            }

            out.println("</table>");
            out.println("</div>");

            out.println("</div>");
            out.println("</body></html>");

        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }
}

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
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;

public class DashboardServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = response.getWriter();

        try {
            QuartoDAO quartoDao = new QuartoDAO();
            ClienteDAO clienteDao = new ClienteDAO();
            ReservaDAO reservaDao = new ReservaDAO();

            List<Quarto> quartos = quartoDao.listarTodos();
            List<Cliente> clientes = clienteDao.listarTodos();
            List<Reserva> reservas = reservaDao.listarTodas();

            long quartosDisponiveis = quartos.stream().filter(q -> "disponivel".equals(q.getStatus())).count();
            long quartosOcupados = quartos.stream().filter(q -> "ocupado".equals(q.getStatus())).count();
            long quartosManutencao = quartos.stream().filter(q -> "manutencao".equals(q.getStatus())).count();
            long reservasAtivas = reservas.stream().filter(r -> "ativa".equals(r.getStatus())).count();

            double receitaAtivas = reservas.stream()
                    .filter(r -> "ativa".equals(r.getStatus()))
                    .mapToDouble(Reserva::getValorTotal)
                    .sum();

            HttpSession session = request.getSession(false);
            String nomeUsuario = (session != null && session.getAttribute("usuarioNome") != null)
                    ? (String) session.getAttribute("usuarioNome") : "Usuário";

            out.println("<!DOCTYPE html>");
            out.println("<html lang='pt'>");
            out.println("<head><meta charset='UTF-8'><title>Dashboard - Kelmira Residencial</title>");
            out.println("<link rel='stylesheet' href='css/dashboard.css'>");
            out.println("<link rel='stylesheet' href='css/quartos.css'></head>");
            out.println("<body>");

            // Sidebar
            out.println("<div class='sidebar'>");
            out.println("<h2>Kelmira Residencial</h2>");
            out.println("<nav>");
            out.println("<a href='dashboard' class='active'>🏠 Início</a>");
            out.println("<a href='quartos'>🛏️ Quartos</a>");
            out.println("<a href='clientes'>👤 Clientes</a>");
            out.println("<a href='reservas'>📅 Reservas</a>");
            out.println("<a href='logout'>🚪 Sair</a>");
            out.println("</nav>");
            out.println("</div>");

            out.println("<div class='main-content'>");
            out.println("<header><h1>Bem-vindo(a), " + nomeUsuario + "!</h1></header>");

            // Cards de resumo
            out.println("<section class='cards'>");

            out.println("<div class='card'><h3>Quartos Disponíveis</h3><p class='numero'>" + quartosDisponiveis + "</p></div>");
            out.println("<div class='card'><h3>Quartos Ocupados</h3><p class='numero'>" + quartosOcupados + "</p></div>");
            out.println("<div class='card'><h3>Em Manutenção</h3><p class='numero'>" + quartosManutencao + "</p></div>");
            out.println("<div class='card'><h3>Reservas Ativas</h3><p class='numero'>" + reservasAtivas + "</p></div>");
            out.println("<div class='card'><h3>Clientes Registados</h3><p class='numero'>" + clientes.size() + "</p></div>");
            out.println("<div class='card'><h3>Receita (Reservas Ativas)</h3><p class='numero'>" + String.format("%.2f", receitaAtivas) + " MT</p></div>");

            out.println("</section>");

            // Ações rápidas
            out.println("<section class='acoes'>");
            out.println("<h2>Ações Rápidas</h2>");
            out.println("<a href='quartos' class='botao'>+ Novo Quarto</a>");
            out.println("<a href='clientes' class='botao'>+ Novo Cliente</a>");
            out.println("<a href='reservas' class='botao'>+ Nova Reserva</a>");
            out.println("</section>");

            // Reservas recentes
            out.println("<section class='acoes' style='margin-top:20px;'>");
            out.println("<h2>Reservas Recentes</h2>");

            if (reservas.isEmpty()) {
                out.println("<p>Nenhuma reserva registada ainda.</p>");
            } else {
                out.println("<table style='width:100%; border-collapse: collapse;'>");
                out.println("<tr><th style='text-align:left; padding:8px; border-bottom:1px solid #eee;'>Cliente</th>" +
                             "<th style='text-align:left; padding:8px; border-bottom:1px solid #eee;'>Quarto</th>" +
                             "<th style='text-align:left; padding:8px; border-bottom:1px solid #eee;'>Entrada</th>" +
                             "<th style='text-align:left; padding:8px; border-bottom:1px solid #eee;'>Saída</th>" +
                             "<th style='text-align:left; padding:8px; border-bottom:1px solid #eee;'>Status</th></tr>");

                int limite = Math.min(5, reservas.size());
                for (int i = 0; i < limite; i++) {
                    Reserva r = reservas.get(i);
                    out.println("<tr>");
                    out.println("<td style='padding:8px; border-bottom:1px solid #eee;'>" + r.getClienteNome() + "</td>");
                    out.println("<td style='padding:8px; border-bottom:1px solid #eee;'>" + r.getQuartoNumero() + "</td>");
                    out.println("<td style='padding:8px; border-bottom:1px solid #eee;'>" + r.getDataEntrada() + "</td>");
                    out.println("<td style='padding:8px; border-bottom:1px solid #eee;'>" + r.getDataSaida() + "</td>");
                    out.println("<td style='padding:8px; border-bottom:1px solid #eee;'><span class='status " + r.getStatus() + "'>" + r.getStatus() + "</span></td>");
                    out.println("</tr>");
                }
                out.println("</table>");
            }
            out.println("</section>");

            out.println("</div>");
            out.println("</body></html>");

        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }
}

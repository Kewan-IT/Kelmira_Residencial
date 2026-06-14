package mz.kelmira.servlet;

import mz.kelmira.dao.QuartoDAO;
import mz.kelmira.model.Quarto;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;

public class QuartoServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String acao = request.getParameter("acao");
        QuartoDAO dao = new QuartoDAO();

        try {
            if ("excluir".equals(acao)) {
                int id = Integer.parseInt(request.getParameter("id"));
                dao.remover(id);
                request.getRequestDispatcher("/quartos").forward(request, response);
                return;
            }

            if ("editar".equals(acao)) {
                int id = Integer.parseInt(request.getParameter("id"));
                Quarto q = dao.buscarPorId(id);
                request.setAttribute("quarto", q);
            }

            List<Quarto> lista = dao.listarTodos();
            request.setAttribute("quartos", lista);
            renderizarPagina(request, response, lista, (Quarto) request.getAttribute("quarto"));

        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idParam = request.getParameter("id");
        String numero = request.getParameter("numero");
        String tipo = request.getParameter("tipo");
        double preco = Double.parseDouble(request.getParameter("preco"));
        String status = request.getParameter("status");
        Quarto q = new Quarto();
        q.setNumero(numero);
        q.setTipo(tipo);
        q.setPreco(preco);
        q.setStatus(status);
        QuartoDAO dao = new QuartoDAO();
        try {
            if (idParam != null && !idParam.isEmpty()) {
                q.setId(Integer.parseInt(idParam));
                dao.atualizar(q);
            } else {
                dao.inserir(q);
            }
            request.getRequestDispatcher("/quartos").forward(request, response);
        } catch (java.sql.SQLIntegrityConstraintViolationException e) {
            try {
                List<Quarto> lista = dao.listarTodos();
                request.setAttribute("erro", "Ja existe um quarto com o numero " + numero + ".");
                renderizarPagina(request, response, lista, q);
            } catch (SQLException ex) {
                throw new ServletException(ex);
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    private void renderizarPagina(HttpServletRequest request, HttpServletResponse response,
                                   List<Quarto> quartos, Quarto editar) throws IOException {

        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = response.getWriter();

        out.println("<!DOCTYPE html>");
        out.println("<html lang='pt'>");
        out.println("<head><meta charset='UTF-8'><title>Quartos - Kelmira Residencial</title>");
        out.println("<link rel='stylesheet' href='css/dashboard.css'>");
        out.println("<link rel='stylesheet' href='css/quartos.css'></head>");
        out.println("<body>");

        out.println("<div class='sidebar'>");
        out.println("<h2>Kelmira Residencial</h2>");
        out.println("<nav>");
        out.println("<a href='dashboard.html'>🏠 Início</a>");
        out.println("<a href='quartos' class='active'>🛏️ Quartos</a>");
        out.println("<a href='clientes.html'>👤 Clientes</a>");
        out.println("<a href='reservas.html'>📅 Reservas</a>");
        out.println("<a href='logout'>🚪 Sair</a>");
        out.println("</nav>");
        out.println("</div>");

        out.println("<div class='main-content'>");
        out.println("<h1>Gestão de Quartos</h1>");

        // Formulário
        out.println("<div class='form-card'>");
        out.println("<h2>" + (editar != null ? "Editar Quarto" : "Novo Quarto") + "</h2>");
        Object erro = request.getAttribute("erro");
        if (erro != null) {
            out.println("<p style=\"color:red; font-weight:bold;\">" + erro + "</p>");
        }
        out.println("<form action='/quartos' method='post'>");

        if (editar != null) {
            out.println("<input type='hidden' name='id' value='" + editar.getId() + "'>");
        }

        out.println("<label>Número do Quarto</label>");
        out.println("<input type='text' name='numero' value='" + (editar != null ? editar.getNumero() : "") + "' required>");

        out.println("<label>Tipo</label>");
        out.println("<select name='tipo' required>");
        String[] tipos = {"Standard", "Luxo", "Suite", "Familiar"};
        for (String tipo : tipos) {
            String sel = (editar != null && editar.getTipo().equals(tipo)) ? "selected" : "";
            out.println("<option value='" + tipo + "' " + sel + ">" + tipo + "</option>");
        }
        out.println("</select>");

        out.println("<label>Preço por noite (MT)</label>");
        out.println("<input type='number' step='0.01' name='preco' value='" + (editar != null ? editar.getPreco() : "") + "' required>");

        out.println("<label>Status</label>");
        out.println("<select name='status' required>");
        String[] statusList = {"disponivel", "ocupado", "manutencao"};
        for (String s : statusList) {
            String sel = (editar != null && editar.getStatus().equals(s)) ? "selected" : "";
            out.println("<option value='" + s + "' " + sel + ">" + s + "</option>");
        }
        out.println("</select>");

        out.println("<button type='submit'>" + (editar != null ? "Salvar Alterações" : "Adicionar Quarto") + "</button>");
        out.println("</form>");
        out.println("</div>");

        // Tabela
        out.println("<div class='table-card'>");
        out.println("<h2>Lista de Quartos</h2>");
        out.println("<table>");
        out.println("<tr><th>Número</th><th>Tipo</th><th>Preço (MT)</th><th>Status</th><th>Ações</th></tr>");

        for (Quarto q : quartos) {
            out.println("<tr>");
            out.println("<td>" + q.getNumero() + "</td>");
            out.println("<td>" + q.getTipo() + "</td>");
            out.println("<td>" + String.format("%.2f", q.getPreco()) + "</td>");
            out.println("<td><span class='status " + q.getStatus() + "'>" + q.getStatus() + "</span></td>");
            out.println("<td>");
            out.println("<a href='quartos?acao=editar&id=" + q.getId() + "'>Editar</a> | ");
            out.println("<a href='quartos?acao=excluir&id=" + q.getId() + "' onclick=\"return confirm('Tem certeza que deseja excluir?')\">Excluir</a>");
            out.println("</td>");
            out.println("</tr>");
        }

        out.println("</table>");
        out.println("</div>");

        out.println("</div>");
        out.println("</body></html>");
    }
}

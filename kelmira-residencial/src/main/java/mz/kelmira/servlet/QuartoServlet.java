package mz.kelmira.servlet;

import mz.kelmira.dao.QuartoDAO;
import mz.kelmira.model.Quarto;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.List;

@jakarta.servlet.annotation.MultipartConfig
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
                response.setStatus(302);
            response.setHeader("Location", "/quartos");
                return;
            }

            Quarto editar = null;
            if ("editar".equals(acao)) {
                int id = Integer.parseInt(request.getParameter("id"));
                editar = dao.buscarPorId(id);
            }

            List<Quarto> lista = dao.listarTodos();
            renderizarPagina(request, response, lista, editar);

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

        String nomeImagem = "default.jpg";
        Part filePart = request.getPart("imagem");

        if (filePart != null && filePart.getSize() > 0) {
            String original = filePart.getSubmittedFileName();
            String ext = original.substring(original.lastIndexOf("."));
            nomeImagem = "quarto_" + numero + ext;

            String uploadDir = getServletContext().getRealPath("/img/quartos");
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            try (InputStream input = filePart.getInputStream()) {
                Files.copy(input, uploadPath.resolve(nomeImagem), StandardCopyOption.REPLACE_EXISTING);
            }
        } else if (idParam != null && !idParam.isEmpty()) {
            nomeImagem = null;
        }

        Quarto q = new Quarto();
        q.setNumero(numero);
        q.setTipo(tipo);
        q.setPreco(preco);
        q.setStatus(status);

        QuartoDAO dao = new QuartoDAO();

        try {
            if (idParam != null && !idParam.isEmpty()) {
                q.setId(Integer.parseInt(idParam));
                if (nomeImagem == null) {
                    Quarto existente = dao.buscarPorId(q.getId());
                    nomeImagem = existente.getImagem();
                }
                q.setImagem(nomeImagem);
                dao.atualizar(q);
            } else {
                q.setImagem(nomeImagem);
                dao.inserir(q);
            }
            response.setStatus(302);
            response.setHeader("Location", "/quartos");

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

        // Sidebar
        out.println("<div class='sidebar'>");
        out.println("<h2>Kelmira Residencial</h2>");
        out.println("<nav>");
        out.println("<a href='dashboard'>🏠 Início</a>");
        out.println("<a href='quartos' class='active'>🛏️ Quartos</a>");
        out.println("<a href='clientes'>👤 Clientes</a>");
        out.println("<a href='reservas'>📅 Reservas</a>");
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

        out.println("<form action='/quartos' method='post' enctype='multipart/form-data'>");

        if (editar != null) {
            out.println("<input type='hidden' name='id' value='" + editar.getId() + "'>");
        }

        out.println("<label>Número do Quarto</label>");
        out.println("<input type='text' name='numero' value='" + (editar != null ? editar.getNumero() : "") + "' required>");

        out.println("<label>Foto do Quarto</label>");
        out.println("<input type='file' name='imagem' accept='image/*'>");

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

        // Cards de Quartos
        out.println("<h2 style=\"margin-bottom:15px; color:#2c3e50;\">Lista de Quartos</h2>");
        out.println("<div class='quartos-grid'>");

        for (Quarto q : quartos) {
            out.println("<div class='quarto-card'>");
            out.println("<img src='img/quartos/" + q.getImagem() + "' alt='Quarto " + q.getNumero() + "' class='quarto-img'>");
            out.println("<div class='quarto-info'>");
            out.println("<h3>Quarto " + q.getNumero() + "</h3>");
            out.println("<p class='tipo'>" + q.getTipo() + "</p>");
            out.println("<p class='preco'>" + String.format("%.2f", q.getPreco()) + " MT / noite</p>");
            out.println("<span class='status " + q.getStatus() + "'>" + q.getStatus() + "</span>");
            out.println("<div class='quarto-acoes'>");
            out.println("<a href='quartos?acao=editar&id=" + q.getId() + "'>Editar</a>");
            out.println("<a href='quartos?acao=excluir&id=" + q.getId() + "' onclick=\"return confirm('Tem certeza que deseja excluir?')\">Excluir</a>");
            out.println("</div>");
            out.println("</div>");
            out.println("</div>");
        }

        out.println("</div>"); // fecha quartos-grid
        out.println("</div>"); // fecha main-content

        out.println("</body></html>");
    }
}

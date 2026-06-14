package mz.kelmira.servlet;

import mz.kelmira.dao.ClienteDAO;
import mz.kelmira.model.Cliente;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;

public class ClienteServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String acao = request.getParameter("acao");
        ClienteDAO dao = new ClienteDAO();

        try {
            if ("excluir".equals(acao)) {
                int id = Integer.parseInt(request.getParameter("id"));
                dao.remover(id);
                response.setStatus(302);
                response.setHeader("Location", "/clientes");
                return;
            }

            Cliente editar = null;
            if ("editar".equals(acao)) {
                int id = Integer.parseInt(request.getParameter("id"));
                editar = dao.buscarPorId(id);
            }

            List<Cliente> lista = dao.listarTodos();
            renderizarPagina(request, response, lista, editar);

        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String idParam = request.getParameter("id");
        String tipo = request.getParameter("tipo");
        String nome = request.getParameter("nome");
        String bi = request.getParameter("bi");
        String nuit = request.getParameter("nuit");
        String representante = request.getParameter("representante");
        String telefone = request.getParameter("telefone");
        String email = request.getParameter("email");
        String endereco = request.getParameter("endereco");

        // Normalizar: campos vazios -> null
        if (bi != null && bi.trim().isEmpty()) bi = null;
        if (nuit != null && nuit.trim().isEmpty()) nuit = null;
        if (representante != null && representante.trim().isEmpty()) representante = null;

        Cliente c = new Cliente();
        c.setTipo(tipo);
        c.setNome(nome);
        c.setBi(bi);
        c.setNuit(nuit);
        c.setRepresentante(representante);
        c.setTelefone(telefone);
        c.setEmail(email);
        c.setEndereco(endereco);

        ClienteDAO dao = new ClienteDAO();

        try {
            if (idParam != null && !idParam.isEmpty()) {
                c.setId(Integer.parseInt(idParam));
                dao.atualizar(c);
            } else {
                dao.inserir(c);
            }
            response.setStatus(302);
            response.setHeader("Location", "/clientes");

        } catch (java.sql.SQLIntegrityConstraintViolationException e) {
            try {
                List<Cliente> lista = dao.listarTodos();
                request.setAttribute("erro", "Já existe um cliente com este BI ou NUIT.");
                renderizarPagina(request, response, lista, c);
            } catch (SQLException ex) {
                throw new ServletException(ex);
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    private void renderizarPagina(HttpServletRequest request, HttpServletResponse response,
                                   List<Cliente> clientes, Cliente editar) throws IOException {

        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = response.getWriter();

        boolean isInstitucional = editar != null && "institucional".equals(editar.getTipo());

        out.println("<!DOCTYPE html>");
        out.println("<html lang='pt'>");
        out.println("<head><meta charset='UTF-8'><title>Clientes - Kelmira Residencial</title>");
        out.println("<link rel='stylesheet' href='css/dashboard.css'>");
        out.println("<link rel='stylesheet' href='css/quartos.css'>");
        out.println("<script>");
        out.println("function alternarTipo() {");
        out.println("  var tipo = document.getElementById('tipoCliente').value;");
        out.println("  var camposSingular = document.getElementsByClassName('campo-singular');");
        out.println("  var camposInstitucional = document.getElementsByClassName('campo-institucional');");
        out.println("  for (var i = 0; i < camposSingular.length; i++) {");
        out.println("    camposSingular[i].style.display = (tipo === 'singular') ? 'block' : 'none';");
        out.println("  }");
        out.println("  for (var i = 0; i < camposInstitucional.length; i++) {");
        out.println("    camposInstitucional[i].style.display = (tipo === 'institucional') ? 'block' : 'none';");
        out.println("  }");
        out.println("  document.getElementById('labelNome').innerText = (tipo === 'institucional') ? 'Nome da Instituição' : 'Nome Completo';");
        out.println("}");
        out.println("</script>");
        out.println("</head>");
        out.println("<body onload='alternarTipo()'>");

        out.println("<div class='sidebar'>");
        out.println("<h2>Kelmira Residencial</h2>");
        out.println("<nav>");
        out.println("<a href='dashboard'>🏠 Início</a>");
        out.println("<a href='quartos'>🛏️ Quartos</a>");
        out.println("<a href='clientes' class='active'>👤 Clientes</a>");
        out.println("<a href='reservas'>📅 Reservas</a>");
        out.println("<a href='logout'>🚪 Sair</a>");
        out.println("</nav>");
        out.println("</div>");

        out.println("<div class='main-content'>");
        out.println("<h1>Gestão de Clientes</h1>");

        // Formulário
        out.println("<div class='form-card'>");
        out.println("<h2>" + (editar != null ? "Editar Cliente" : "Novo Cliente") + "</h2>");

        Object erro = request.getAttribute("erro");
        if (erro != null) {
            out.println("<p style=\"color:red; font-weight:bold;\">" + erro + "</p>");
        }

        out.println("<form action='/clientes' method='post'>");

        if (editar != null) {
            out.println("<input type='hidden' name='id' value='" + editar.getId() + "'>");
        }

        // Tipo de cliente
        out.println("<label>Tipo de Cliente</label>");
        out.println("<select name='tipo' id='tipoCliente' onchange='alternarTipo()' required>");
        out.println("<option value='singular'" + (!isInstitucional ? " selected" : "") + ">Singular (Pessoa Física)</option>");
        out.println("<option value='institucional'" + (isInstitucional ? " selected" : "") + ">Institucional (Empresa)</option>");
        out.println("</select>");

        // Nome (label dinâmico)
        out.println("<label id='labelNome'>Nome Completo</label>");
        out.println("<input type='text' name='nome' value='" + (editar != null ? editar.getNome() : "") + "' required>");

        // Campos exclusivos Singular: BI
        out.println("<div class='campo-singular'>");
        out.println("<label>Número do BI</label>");
        out.println("<input type='text' name='bi' value='" + (editar != null && editar.getBi() != null ? editar.getBi() : "") + "'>");
        out.println("</div>");

        // Campos exclusivos Institucional: NUIT + Representante
        out.println("<div class='campo-institucional'>");
        out.println("<label>NUIT</label>");
        out.println("<input type='text' name='nuit' value='" + (editar != null && editar.getNuit() != null ? editar.getNuit() : "") + "'>");
        out.println("<label>Nome do Representante</label>");
        out.println("<input type='text' name='representante' value='" + (editar != null && editar.getRepresentante() != null ? editar.getRepresentante() : "") + "'>");
        out.println("</div>");

        // Campos comuns
        out.println("<label>Telefone</label>");
        out.println("<input type='text' name='telefone' value='" + (editar != null && editar.getTelefone() != null ? editar.getTelefone() : "") + "'>");

        out.println("<label>Email</label>");
        out.println("<input type='email' name='email' value='" + (editar != null && editar.getEmail() != null ? editar.getEmail() : "") + "'>");

        out.println("<label>Endereço</label>");
        out.println("<input type='text' name='endereco' value='" + (editar != null && editar.getEndereco() != null ? editar.getEndereco() : "") + "'>");

        out.println("<button type='submit'>" + (editar != null ? "Salvar Alterações" : "Adicionar Cliente") + "</button>");
        out.println("</form>");
        out.println("</div>");

        // Tabela
        out.println("<div class='table-card'>");
        out.println("<h2>Lista de Clientes</h2>");
        out.println("<table>");
        out.println("<tr><th>Tipo</th><th>Nome</th><th>BI / NUIT</th><th>Telefone</th><th>Email</th><th>Ações</th></tr>");

        for (Cliente c : clientes) {
            boolean inst = "institucional".equals(c.getTipo());
            out.println("<tr>");
            out.println("<td>" + (inst ? "Institucional" : "Singular") + "</td>");
            out.println("<td>" + c.getNome() + (inst && c.getRepresentante() != null ? "<br><small>Rep: " + c.getRepresentante() + "</small>" : "") + "</td>");
            out.println("<td>" + (inst ? (c.getNuit() != null ? c.getNuit() : "-") : (c.getBi() != null ? c.getBi() : "-")) + "</td>");
            out.println("<td>" + (c.getTelefone() != null ? c.getTelefone() : "-") + "</td>");
            out.println("<td>" + (c.getEmail() != null ? c.getEmail() : "-") + "</td>");
            out.println("<td>");
            out.println("<a href='clientes?acao=editar&id=" + c.getId() + "'>Editar</a> | ");
            out.println("<a href='clientes?acao=excluir&id=" + c.getId() + "' onclick=\"return confirm('Tem certeza que deseja excluir?')\">Excluir</a>");
            out.println("</td>");
            out.println("</tr>");
        }

        out.println("</table>");
        out.println("</div>");

        out.println("</div>");
        out.println("</body></html>");
    }
}

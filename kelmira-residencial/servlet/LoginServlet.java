package mz.kelmira.servlet;

import mz.kelmira.util.ConexaoDB;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String username = request.getParameter("username");
        String senha = request.getParameter("senha");

        String sql = "SELECT * FROM usuarios WHERE username = ? AND senha = ?";

        try (Connection conn = ConexaoDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, senha);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                HttpSession session = request.getSession();
                session.setAttribute("usuarioNome", rs.getString("nome"));
                session.setAttribute("usuarioCargo", rs.getString("cargo"));

                response.sendRedirect("dashboard.html");
            } else {
                request.setAttribute("erro", "Usuário ou senha incorretos!");
                request.getRequestDispatcher("login.html").forward(request, response);
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().println("Erro ao conectar ao banco de dados: " + e.getMessage());
        }
    }
}
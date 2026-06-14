package mz.kelmira.servlet;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

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

import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

public class ComprovanteServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            int reservaId = Integer.parseInt(request.getParameter("id"));

            ReservaDAO reservaDao = new ReservaDAO();
            ClienteDAO clienteDao = new ClienteDAO();
            QuartoDAO quartoDao = new QuartoDAO();

            Reserva reserva = reservaDao.buscarPorId(reservaId);
            if (reserva == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Reserva não encontrada");
                return;
            }

            Cliente cliente = clienteDao.buscarPorId(reserva.getClienteId());
            Quarto quarto = quartoDao.buscarPorId(reserva.getQuartoId());

            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "inline; filename=comprovante_reserva_" + reserva.getId() + ".pdf");

            OutputStream out = response.getOutputStream();
            Document document = new Document(PageSize.A4, 50, 50, 50, 50);
            PdfWriter.getInstance(document, out);
            document.open();

            // Cores
            Color corPrimaria = new Color(44, 62, 80); // #2c3e50

            // Cabeçalho
            Font fonteTitulo = new Font(Font.HELVETICA, 22, Font.BOLD, corPrimaria);
            Paragraph titulo = new Paragraph("Kelmira Residencial", fonteTitulo);
            titulo.setAlignment(Element.ALIGN_CENTER);
            document.add(titulo);

            Font fonteSubtitulo = new Font(Font.HELVETICA, 12, Font.NORMAL, Color.GRAY);
            Paragraph subtitulo = new Paragraph("Comprovante de Reserva", fonteSubtitulo);
            subtitulo.setAlignment(Element.ALIGN_CENTER);
            subtitulo.setSpacingAfter(20);
            document.add(subtitulo);

            // Linha separadora
            document.add(new Paragraph(" "));

            Font fonteLabel = new Font(Font.HELVETICA, 11, Font.BOLD);
            Font fonteValor = new Font(Font.HELVETICA, 11, Font.NORMAL);

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

            // Tabela de detalhes
            PdfPTable tabela = new PdfPTable(2);
            tabela.setWidthPercentage(100);
            tabela.setSpacingBefore(10);
            tabela.setWidths(new float[]{1f, 2f});

            adicionarLinha(tabela, "Nº da Reserva:", "#" + reserva.getId(), fonteLabel, fonteValor);
            adicionarLinha(tabela, "Cliente:", cliente.getNome(), fonteLabel, fonteValor);

            if ("institucional".equals(cliente.getTipo())) {
                adicionarLinha(tabela, "NUIT:", cliente.getNuit() != null ? cliente.getNuit() : "-", fonteLabel, fonteValor);
                if (cliente.getRepresentante() != null) {
                    adicionarLinha(tabela, "Representante:", cliente.getRepresentante(), fonteLabel, fonteValor);
                }
            } else {
                adicionarLinha(tabela, "BI:", cliente.getBi() != null ? cliente.getBi() : "-", fonteLabel, fonteValor);
            }

            adicionarLinha(tabela, "Telefone:", cliente.getTelefone() != null ? cliente.getTelefone() : "-", fonteLabel, fonteValor);
            adicionarLinha(tabela, "Email:", cliente.getEmail() != null ? cliente.getEmail() : "-", fonteLabel, fonteValor);

            tabela.addCell(criarCelulaVazia());
            tabela.addCell(criarCelulaVazia());

            adicionarLinha(tabela, "Quarto:", "Nº " + quarto.getNumero() + " - " + quarto.getTipo(), fonteLabel, fonteValor);
            adicionarLinha(tabela, "Data de Entrada:", sdf.format(reserva.getDataEntrada()), fonteLabel, fonteValor);
            adicionarLinha(tabela, "Data de Saída:", sdf.format(reserva.getDataSaida()), fonteLabel, fonteValor);
            adicionarLinha(tabela, "Valor Total:", String.format("%.2f MT", reserva.getValorTotal()), fonteLabel, fonteValor);
            adicionarLinha(tabela, "Status:", reserva.getStatus().toUpperCase(), fonteLabel, fonteValor);

            document.add(tabela);

            // Aviso
            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));

            Font fonteAviso = new Font(Font.HELVETICA, 10, Font.ITALIC, Color.DARK_GRAY);
            Paragraph aviso = new Paragraph(
                "Por favor, apresente este comprovante (impresso ou digital) na recepção no dia da sua chegada.",
                fonteAviso
            );
            aviso.setAlignment(Element.ALIGN_CENTER);
            aviso.setSpacingBefore(20);
            document.add(aviso);

            // Rodapé
            document.add(new Paragraph(" "));
            Font fonteRodape = new Font(Font.HELVETICA, 9, Font.NORMAL, Color.GRAY);
            Paragraph rodape = new Paragraph("Kelmira Residencial - Documento gerado automaticamente.", fonteRodape);
            rodape.setAlignment(Element.ALIGN_CENTER);
            rodape.setSpacingBefore(30);
            document.add(rodape);

            document.close();

        } catch (SQLException | DocumentException e) {
            throw new ServletException(e);
        }
    }

    private void adicionarLinha(PdfPTable tabela, String label, String valor, Font fonteLabel, Font fonteValor) {
        PdfPCell cellLabel = new PdfPCell(new Phrase(label, fonteLabel));
        cellLabel.setBorder(Rectangle.NO_BORDER);
        cellLabel.setPaddingBottom(6);
        tabela.addCell(cellLabel);

        PdfPCell cellValor = new PdfPCell(new Phrase(valor, fonteValor));
        cellValor.setBorder(Rectangle.NO_BORDER);
        cellValor.setPaddingBottom(6);
        tabela.addCell(cellValor);
    }

    private PdfPCell criarCelulaVazia() {
        PdfPCell cell = new PdfPCell(new Phrase(" "));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setFixedHeight(8);
        return cell;
    }
}

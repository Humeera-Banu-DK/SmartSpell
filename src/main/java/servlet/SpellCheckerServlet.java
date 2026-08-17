package servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/check")
public class SpellCheckerServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;


    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        String word =
                request.getParameter("word");

        response.setContentType(
                "application/json"
        );

        response.setCharacterEncoding(
                "UTF-8"
        );

        PrintWriter out =
                response.getWriter();

        if (word == null ||
                word.trim().isEmpty()) {

            out.print("[]");
            return;
        }

        List<String> suggestions =
                SpellChecker.getSuggestions(
                        word,
                        5
                );

        StringBuilder json =
                new StringBuilder("[");

        for (int i = 0;
             i < suggestions.size();
             i++) {

            if (i > 0) {
                json.append(",");
            }

            json.append("\"")
                    .append(
                            escapeJson(
                                    suggestions.get(i)
                            )
                    )
                    .append("\"");
        }

        json.append("]");

        out.print(json.toString());
    }


    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding(
                "UTF-8"
        );

        String text =
                request.getParameter("text");

        response.setContentType(
                "text/plain"
        );

        response.setCharacterEncoding(
                "UTF-8"
        );

        PrintWriter out =
                response.getWriter();

        if (text == null ||
                text.trim().isEmpty()) {

            out.print("");
            return;
        }

        String correctedText =
                SpellChecker.checkText(text);

        out.print(correctedText);
    }


    private String escapeJson(
            String value) {

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}
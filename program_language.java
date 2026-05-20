import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;

public class program_language {

    static class Variabele {
        String naam;
        String waarde;

        public Variabele(String naam, String waarde) {
            this.naam = naam;
            this.waarde = waarde;
        }
    }

    static Map<String, Variabele> vars = new HashMap<>();
    static Stack<Boolean> ifStack = new Stack<>();

    public static void main(String[] args) {
        JFrame frame = new JFrame("TobiScript Editor");
        frame.setSize(700, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JTextArea codeArea = new JTextArea();
        codeArea.setFont(new Font("Consolas", Font.PLAIN, 16));

        JTextArea console = new JTextArea();
        console.setEditable(false);
        console.setFont(new Font("Consolas", Font.PLAIN, 16));

        JButton runButton = new JButton("Run");

        runButton.addActionListener(e -> {
            console.setText("");
            String code = codeArea.getText();
            String[] lines = code.split("\n");

            ifStack.clear();
            vars.clear();

            for (String line : lines) {
                String output = runLine(line);
                if (!output.isEmpty()) {
                    console.append(output + "\n");
                }
            }
        });

        JSplitPane split = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(codeArea),
                new JScrollPane(console)
        );
        split.setDividerLocation(300);

        frame.add(split, BorderLayout.CENTER);
        frame.add(runButton, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    static int countLeadingColons(String line) {
        int count = 0;
        while (count < line.length() && line.charAt(count) == ':') {
            count++;
        }
        return count;
    }

    static String runLine(String rawLine) {
        if (rawLine.trim().isEmpty()) return "";

        int depth = countLeadingColons(rawLine);
        String cleanLine = rawLine.substring(depth).trim();
        if (cleanLine.isEmpty()) return "";

        String[] code = cleanLine.split(" +");

        String cmd = code[0];

        // active only if all ifStack conditions are true
        boolean active = true;
        for (boolean cond : ifStack) {
            if (!cond) {
                active = false;
                break;
            }
        }

        // IF
        if (cmd.equals("if")) {
            if (code.length < 4) return "Error: invalid IF syntax";

            String left = code[1];
            String op   = code[2];
            String right= code[3];

            if (vars.containsKey(left))  left  = vars.get(left).waarde;
            if (vars.containsKey(right)) right = vars.get(right).waarde;

            boolean condition;
            switch (op) {
                case "==":
                    condition = left.equals(right);
                    break;
                case "!=":
                    condition = !left.equals(right);
                    break;
                default:
                    return "Error: unknown operator " + op;
            }

            ifStack.push(condition);
            return "";
        }

        // ENDIF
        if (cmd.equals("endif")) {
            if (ifStack.isEmpty()) return "Error: endif without if";
            ifStack.pop();
            return "";
        }

        // if parent IFs are false, skip this line
        if (!active) return "";

        // VAR
        if (cmd.equals("var")) {
            if (code.length < 3) return "Error: var needs name and value";

            String name  = code[1];
            String value = code[2];

            if (value.equals("input")) {
                value = JOptionPane.showInputDialog("Enter " + name + ":");
            }

            vars.put(name, new Variabele(name, value));
            return "";
        }

        // PRINT
        if (cmd.equals("print")) {
            return resolveWords(code, 1);
        }

        // VARIABLE UPDATE
        if (vars.containsKey(cmd)) {
            if (code.length < 2) return "Error: missing new value";

            String varName  = cmd;
            String newValue = code[1];

            if (newValue.equals("input")) {
                newValue = JOptionPane.showInputDialog("Enter " + varName + ":");
            }

            vars.get(varName).waarde = newValue;
            return "";
        }

        return "Error: unknown command → " + cleanLine;
    }

    static String resolveWords(String[] code, int startIndex) {
        StringBuilder out = new StringBuilder();

        List<String> changers = Arrays.asList("+", "-", "x", ":");

        for (int i = startIndex; i < code.length; i++) {

            // SAFETY: check if we have at least 3 tokens ahead
            if (i + 2 < code.length) {
                String t1 = code[i];
                String t2 = code[i + 1];
                String t3 = code[i + 2];

                // Replace variables with their values
                if (vars.containsKey(t1)) t1 = vars.get(t1).waarde;
                if (vars.containsKey(t3)) t3 = vars.get(t3).waarde;

                // Check pattern: number operator number
                if (isNumeric(t1) && changers.contains(t2) && isNumeric(t3)) {

                    int a = Integer.parseInt(t1);
                    int b = Integer.parseInt(t3);
                    int result = 0;

                    switch (t2) {
                        case "+": result = a + b; break;
                        case "-": result = a - b; break;
                        case "x": result = a * b; break;
                        case ":": 
                            if (b == 0) return "Error: division by zero";
                            result = a / b; 
                            break;
                    }

                    out.append(result);

                    // Skip the next 2 tokens because we already processed them
                    i += 2;
                    continue;
                }
            }

            // If not math, print normally
            String word = code[i];

            if (vars.containsKey(word)) {
                out.append(vars.get(word).waarde);
            } else {
                out.append(word);
            }

            if (i < code.length - 1) out.append(" ");
        }

        return out.toString();
    }


    public static boolean isNumeric(String str) {
        if (str == null || str.trim().isEmpty()) {
            return false; // Null or empty string is not numeric
        }
        str = str.trim();

        try {
            Double.parseDouble(str); // Try parsing as a double
            return true;
        } catch (NumberFormatException e) {
            return false; // Parsing failed, not a number
        }
    }
}
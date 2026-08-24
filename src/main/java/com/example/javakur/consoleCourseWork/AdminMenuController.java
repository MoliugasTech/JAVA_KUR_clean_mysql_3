package com.example.javakur.consoleCourseWork;

import com.example.javakur.security.UserSession;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.util.List;

class AdminMenuController extends AbstractRoleMenuController {
    static final List<String> OPTIONS = List.of(
            "Valdyti vartotojus", "Peržiūrėti restoranus",
            "Peržiūrėti visus užsakymus", "Peržiūrėti atsiliepimus");

    AdminMenuController(BufferedReader input, PrintStream output) { super(input, output); }
    public List<String> options() { return OPTIONS; }
    public void handle(UserSession session, int choice) {
        output.println("Administratoriaus funkcijos bus prijungtos M14 etape.");
    }
}

package ru.rage.smlink;

import ru.rage.spoml.*;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

public class Main
{
    static final Charset FILE_CHARSET = StandardCharsets.UTF_8;

    public static void main(String[] args)
    {
        if (args.length < 3)
        {
            System.out.println("Usage: smlink <-s|-d> <-l|-e> <file name w/o ext> [library path]");
            return;
        }

        boolean dynamic = args[0].equals("-d");
        boolean library = args[1].equals("-l");
        Linker linker = new Linker(dynamic);

        try
        {
            Path path = Paths.get(args[2] + Include.FILE_EXT);
            if (Files.exists(path))
                linker.addIncludes(Files.readAllBytes(path));

            path = Paths.get(args[2] + Data.FILE_EXT);
            if (Files.exists(path))
                linker.addData(Files.readAllBytes(path));

            path = Paths.get(args[2] + Command.FILE_EXT);
            if (Files.exists(path))
                linker.addCode(Files.readAllBytes(path));

            path = Paths.get(args[2] + Extern.FILE_EXT);
            if (Files.exists(path))
                linker.addExterns(Files.readAllBytes(path));
        }
        catch (Exception ex)
        {
            System.out.printf("Reading error: %s\n", ex);
            return;
        }

        try
        {
            byte[] output = linker.link((args.length > 3) ? args[3] : "");
            Files.write(Paths.get(args[2] + ((library) ? Extern.LIBRARY_EXT : Command.EXECUTABLE_EXT)),
                        output);
        }
        catch (Exception ex)
        {
            System.out.printf("Linking error: %s\n", ex);
        }
    }
}

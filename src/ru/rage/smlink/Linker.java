package ru.rage.smlink;

import ru.rage.spoml.*;

import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;

public class Linker
{
    private ArrayList<Include> _includes;
    private ArrayList<Byte>    _incs;

    private ArrayList<Byte> _data;
    private ArrayList<Byte> _externs;

    private LinkedList<Byte> _code;

    private boolean _dynamic;

    Linker(boolean dynamic)
    {
        _dynamic = dynamic;
        _incs = null;
        _includes = null;
        _externs = null;
        _data = null;
        _code = new LinkedList<>();
    }

    /**
     * Добавляет включения в зависимости от типа компоновки
     *
     * @param bytes Байты файла включений
     */
    void addIncludes(byte[] bytes)
    {
        if (_dynamic)
        {
            _incs = new ArrayList<>(bytes.length);

            for (byte b : bytes)
                _incs.add(b);
        }
        else
        {
            _includes = new ArrayList<>();
            String s = new String(bytes, Main.FILE_CHARSET);
            String[] includes = s.split("[\\r\\n]+");

            for (String include : includes)
            {
                if (include.length() < 5)
                    continue;

                String[] inc = include.split(" ");
                _includes.add(new Include(inc[0], inc[1], Integer.parseInt(inc[2])));
            }
        }
    }

    void addExterns(byte[] bytes)
    {
        _externs = new ArrayList<>(bytes.length);

        for (byte b : bytes)
            _externs.add(b);
    }

    void addCode(byte[] code)
    {
        for (byte b : code)
            _code.add(b);
    }

    void addData(byte[] data)
    {
        _data = new ArrayList<>(data.length);

        for (byte b : data)
            _data.add(b);
    }

    byte[] link(String libPath) throws Exception
    {
        int incSize, codeSize;
        int dataSize = _data.size();
        int extrnSize = _externs.size();

        if (_dynamic)
        {
            codeSize = _code.size();
            _code.addAll(0, _data);

            if (_incs != null)
            {
                incSize = _incs.size();
                _code.addAll(0, _incs);
            }
            else
                incSize = 0;
        }
        else
        {
            incSize = 0;
            if (_includes != null)
            {
                for (Include inc : _includes)
                {
                    _code.addAll(inc.getAddr(),
                                 extract(Paths.get(libPath, inc.getLib()), inc.getName()));
                }
            }
            codeSize = _code.size();
            _code.addAll(0, _data);
        }
        _code.addAll(_externs);
        ByteBuffer bb = ByteBuffer.allocateDirect(Integer.BYTES * 4);
        bb.order(Command.BYTE_ORDER);
        bb.putInt(incSize);
        bb.putInt(dataSize);
        bb.putInt(codeSize);
        bb.putInt(extrnSize);

        byte[] sizes = bb.array();
        byte[] result = new byte[_code.size() + sizes.length];

        int i = 0;
        for (; i < sizes.length; i++)
            result[i] = sizes[i];
        for (Byte b : _code)
            result[i++] = b;

        return result;
    }

    private ArrayList<Byte> extract(Path lib, String name) throws Exception
    {
        byte[] file = Files.readAllBytes(lib);
        ByteBuffer bb = ByteBuffer.wrap(file, 0, Integer.BYTES * 4);
        bb.order(Command.BYTE_ORDER);

        int externsOffset = bb.capacity();
        for (int i = 0; i < 3; i++)
            externsOffset += bb.getInt();
        int externsSize = bb.getInt();

        String s = new String(file, externsOffset, externsSize);
        String[] externs = s.split("[\\r\\n]+");
        for (String extern : externs)
        {
            if (extern.length() < 5)
                continue;

            String[] e = extern.split(" ");
            if (e[0].equals(name))
            {
                int start = Integer.parseInt(e[1]);
                int end = Integer.parseInt(e[2]);
                ArrayList<Byte> result = new ArrayList<>(end - start);

                for (int i = start; i < end; i++)
                    result.add(file[i]);
                return result;
            }
        }
        throw new Exception(String.format("Extern \"%s\" not found", name));
    }
}
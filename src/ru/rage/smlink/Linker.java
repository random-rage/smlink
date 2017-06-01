package ru.rage.smlink;

import ru.rage.spoml.*;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;

class Linker
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

                _includes.add(new Include(include));
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
        int dataSize = (_data == null) ? 0 : _data.size();
        int extrnSize = (_externs == null) ? 0 : _externs.size();

        if (_dynamic)
        {
            codeSize = _code.size();
            if (_data != null)
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
                    _code.addAll(inc.getAddr(), inc.extractCode(libPath));
            }
            codeSize = _code.size();
            if (_data != null)
                _code.addAll(0, _data);
        }
        if (_externs != null)
            _code.addAll(_externs);

        ByteBuffer bb = ByteBuffer.allocate(Integer.BYTES * 4);
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
}

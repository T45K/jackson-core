package com.fasterxml.jackson.core.async;

import java.io.*;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.JsonParser.NumberType;

public class AsyncScalarArrayTest extends AsyncTestBase
{
    private final JsonFactory JSON_F = new JsonFactory();

    /*
    /**********************************************************************
    /* Simple token (true, false, null) tests
    /**********************************************************************
     */

    public void testTokens() throws IOException
    {
        byte[] data = _jsonDoc("  [ true, false, true, null,false , null]");
        JsonFactory f = JSON_F;

        // first: no offsets
        _testTokens(f, data, 0, 100);
        _testTokens(f, data, 0, 5);
        _testTokens(f, data, 0, 3);
        _testTokens(f, data, 0, 2);
        _testTokens(f, data, 0, 1);

        // then with offsets
        _testTokens(f, data, 1, 100);
        _testTokens(f, data, 1, 3);
        _testTokens(f, data, 1, 1);
    }

    private void _testTokens(JsonFactory f,
            byte[] data, int offset, int readSize) throws IOException
    {
        AsyncReaderWrapper r = asyncForBytes(f, readSize, data, offset);
        // start with "no token"
        assertNull(r.currentToken());
        assertToken(JsonToken.START_ARRAY, r.nextToken());
        assertToken(JsonToken.VALUE_TRUE, r.nextToken());
        assertToken(JsonToken.VALUE_FALSE, r.nextToken());
        assertEquals("false", r.currentText());
        assertEquals("false", r.currentTextViaCharacters());
        assertToken(JsonToken.VALUE_TRUE, r.nextToken());
        assertToken(JsonToken.VALUE_NULL, r.nextToken());
        assertEquals("null", r.currentText());
        assertEquals("null", r.currentTextViaCharacters());
        assertToken(JsonToken.VALUE_FALSE, r.nextToken());
        assertToken(JsonToken.VALUE_NULL, r.nextToken());
        
        assertToken(JsonToken.END_ARRAY, r.nextToken());

        // and end up with "no token" as well
        assertNull(r.nextToken());
        assertTrue(r.isClosed());
    }

    /*
    /**********************************************************************
    /* Int / long tests
    /**********************************************************************
     */

    public void testInts() throws IOException
    {
        final int[] input = new int[] { 1, -1, 16, -17, 0, 131, -0, -155, 1000, -3000, 0xFFFF, -99999,
                Integer.MAX_VALUE, 0, Integer.MIN_VALUE };
        StringBuilder sb = new StringBuilder().append("[");
        for (int i = 0; i < input.length; ++i) {
            if (i > 0) sb.append(',');
            sb.append(input[i]);
        }
        byte[] data = _jsonDoc(sb.append(']').toString());
        JsonFactory f = JSON_F;
        _testInts(f, input, data, 0, 100);
        _testInts(f, input, data, 0, 3);
        _testInts(f, input, data, 0, 1);

        _testInts(f, input, data, 1, 100);
        _testInts(f, input, data, 1, 3);
        _testInts(f, input, data, 1, 1);
    }

    private void _testInts(JsonFactory f, int[] values,
            byte[] data, int offset, int readSize) throws IOException
    {
        AsyncReaderWrapper r = asyncForBytes(f, readSize, data, offset);
        // start with "no token"
        assertNull(r.currentToken());
        assertToken(JsonToken.START_ARRAY, r.nextToken());
        for (int i = 0; i < values.length; ++i) {
            assertToken(JsonToken.VALUE_NUMBER_INT, r.nextToken());
            assertEquals(values[i], r.getIntValue());
            assertEquals(NumberType.INT, r.getNumberType());
        }
        assertToken(JsonToken.END_ARRAY, r.nextToken());

        // and end up with "no token" as well
        assertNull(r.nextToken());
        assertTrue(r.isClosed());
    }

    public void testLong() throws IOException
    {
        final long[] input = new long[] {
                // SmileGenerator will try to minimize so....
//                1, -1, 16, -17, 131, -155, 1000, -3000, 0xFFFF, -99999,
                -1L + Integer.MIN_VALUE, 1L + Integer.MAX_VALUE,
                19L * Integer.MIN_VALUE, 27L * Integer.MAX_VALUE,
                Long.MIN_VALUE, Long.MAX_VALUE };
        ByteArrayOutputStream bytes = new ByteArrayOutputStream(100);
        JsonFactory f = JSON_F;
        JsonGenerator g = f.createGenerator(bytes);
        g.writeStartArray();
        for (int i = 0; i < input.length; ++i) {
            g.writeNumber(input[i]);
        }
        g.writeEndArray();
        g.close();
        byte[] data = bytes.toByteArray();
        _testLong(f, input, data, 0, 100);
        _testLong(f, input, data, 0, 3);
        _testLong(f, input, data, 0, 1);

        _testLong(f, input, data, 1, 100);
        _testLong(f, input, data, 1, 3);
        _testLong(f, input, data, 1, 1);
    }

    private void _testLong(JsonFactory f, long[] values,
            byte[] data, int offset, int readSize) throws IOException
    {
        AsyncReaderWrapper r = asyncForBytes(f, readSize, data, offset);
        // start with "no token"
        assertNull(r.currentToken());
        assertToken(JsonToken.START_ARRAY, r.nextToken());
        for (int i = 0; i < values.length; ++i) {
            assertToken(JsonToken.VALUE_NUMBER_INT, r.nextToken());
            assertEquals(values[i], r.getLongValue());
            assertEquals(NumberType.LONG, r.getNumberType());
        }
        assertToken(JsonToken.END_ARRAY, r.nextToken());

        // and end up with "no token" as well
        assertNull(r.nextToken());
        assertTrue(r.isClosed());
    }

    /*
    /**********************************************************************
    /* Floating point
    /**********************************************************************
     */

/*    public void testFloats() throws IOException
    {
        final float[] input = new float[] { 0.0f, 0.25f, -0.5f, 10000.125f, - 99999.075f };
        ByteArrayOutputStream bytes = new ByteArrayOutputStream(100);
        JsonFactory f = JSON_F;
        JsonGenerator g = f.createGenerator(bytes);
        g.writeStartArray();
        for (int i = 0; i < input.length; ++i) {
            g.writeNumber(input[i]);
        }
        g.writeEndArray();
        g.close();
        byte[] data = bytes.toByteArray();
        _testFloats(f, input, data, 0, 100);
        _testFloats(f, input, data, 0, 3);
        _testFloats(f, input, data, 0, 1);

        _testFloats(f, input, data, 1, 100);
        _testFloats(f, input, data, 1, 3);
        _testFloats(f, input, data, 1, 1);
    }

    private void _testFloats(JsonFactory f, float[] values,
            byte[] data, int offset, int readSize) throws IOException
    {
        AsyncReaderWrapper r = asyncForBytes(f, readSize, data, offset);
        // start with "no token"
        assertNull(r.currentToken());
        assertToken(JsonToken.START_ARRAY, r.nextToken());
        for (int i = 0; i < values.length; ++i) {
            assertToken(JsonToken.VALUE_NUMBER_FLOAT, r.nextToken());
            assertEquals(values[i], r.getFloatValue());
            assertEquals(NumberType.FLOAT, r.getNumberType());
        }
        assertToken(JsonToken.END_ARRAY, r.nextToken());
        // and end up with "no token" as well
        assertNull(r.nextToken());
        assertTrue(r.isClosed());
    }

    public void testDoubles() throws IOException
    {
        final double[] input = new double[] { 0.0, 0.25, -0.5, 10000.125, -99999.075 };
        ByteArrayOutputStream bytes = new ByteArrayOutputStream(100);
        JsonFactory f = JSON_F;
        JsonGenerator g = f.createGenerator(bytes);
        g.writeStartArray();
        for (int i = 0; i < input.length; ++i) {
            g.writeNumber(input[i]);
        }
        g.writeEndArray();
        g.close();
        byte[] data = bytes.toByteArray();
        _testDoubles(f, input, data, 0, 100);
        _testDoubles(f, input, data, 0, 3);
        _testDoubles(f, input, data, 0, 1);

        _testDoubles(f, input, data, 1, 100);
        _testDoubles(f, input, data, 1, 3);
        _testDoubles(f, input, data, 1, 1);
    }

    private void _testDoubles(JsonFactory f, double[] values,
            byte[] data, int offset, int readSize) throws IOException
    {
        AsyncReaderWrapper r = asyncForBytes(f, readSize, data, offset);
        // start with "no token"
        assertNull(r.currentToken());
        assertToken(JsonToken.START_ARRAY, r.nextToken());
        for (int i = 0; i < values.length; ++i) {
            assertToken(JsonToken.VALUE_NUMBER_FLOAT, r.nextToken());
            assertEquals(values[i], r.getDoubleValue());
            assertEquals(NumberType.DOUBLE, r.getNumberType());
        }
        assertToken(JsonToken.END_ARRAY, r.nextToken());

        // and end up with "no token" as well
        assertNull(r.nextToken());
        assertTrue(r.isClosed());
    }
*/
    /*
    /**********************************************************************
    /* BigInteger, BigDecimal
    /**********************************************************************
     */
/*
    public void testBigIntegers() throws IOException
    {
        BigInteger bigBase = BigInteger.valueOf(1234567890344656736L);
        final BigInteger[] input = new BigInteger[] {
                BigInteger.ZERO,
                BigInteger.ONE,
                BigInteger.TEN,
                BigInteger.valueOf(-999L),
                bigBase,
                bigBase.shiftLeft(100).add(BigInteger.valueOf(123456789L)),
                bigBase.add(bigBase),
                bigBase.multiply(BigInteger.valueOf(17)),
                bigBase.negate()
        };
        ByteArrayOutputStream bytes = new ByteArrayOutputStream(100);
        JsonFactory f = JSON_F;
        JsonGenerator g = f.createGenerator(bytes);
        g.writeStartArray();
        for (int i = 0; i < input.length; ++i) {
            g.writeNumber(input[i]);
        }
        g.writeEndArray();
        g.close();
        byte[] data = bytes.toByteArray();
        _testBigIntegers(f, input, data, 0, 100);
        _testBigIntegers(f, input, data, 0, 3);
        _testBigIntegers(f, input, data, 0, 1);

        _testBigIntegers(f, input, data, 1, 100);
        _testBigIntegers(f, input, data, 2, 3);
        _testBigIntegers(f, input, data, 3, 1);
    }

    private void _testBigIntegers(JsonFactory f, BigInteger[] values,
            byte[] data, int offset, int readSize) throws IOException
    {
        AsyncReaderWrapper r = asyncForBytes(f, readSize, data, offset);
        // start with "no token"
        assertNull(r.currentToken());
        assertToken(JsonToken.START_ARRAY, r.nextToken());
        for (int i = 0; i < values.length; ++i) {
            BigInteger expValue = values[i];
            assertToken(JsonToken.VALUE_NUMBER_INT, r.nextToken());
            assertEquals(expValue, r.getBigIntegerValue());
            assertEquals(NumberType.BIG_INTEGER, r.getNumberType());
        }
        assertToken(JsonToken.END_ARRAY, r.nextToken());
        assertNull(r.nextToken());
        assertTrue(r.isClosed());
    }

    public void testBigDecimals() throws IOException
    {
        BigDecimal bigBase = new BigDecimal("1234567890344656736.125");
        final BigDecimal[] input = new BigDecimal[] {
                BigDecimal.ZERO,
                BigDecimal.ONE,
                BigDecimal.TEN,
                BigDecimal.valueOf(-999.25),
                bigBase,
                bigBase.divide(new BigDecimal("5")),
                bigBase.add(bigBase),
                bigBase.multiply(new BigDecimal("1.23")),
                bigBase.negate()
        };
        ByteArrayOutputStream bytes = new ByteArrayOutputStream(100);
        JsonFactory f = JSON_F;
        JsonGenerator g = f.createGenerator(bytes);
        g.writeStartArray();
        for (int i = 0; i < input.length; ++i) {
            g.writeNumber(input[i]);
        }
        g.writeEndArray();
        g.close();
        byte[] data = bytes.toByteArray();

        _testBigDecimals(f, input, data, 0, 100);
        _testBigDecimals(f, input, data, 0, 3);
        _testBigDecimals(f, input, data, 0, 1);

        _testBigDecimals(f, input, data, 1, 100);
        _testBigDecimals(f, input, data, 2, 3);
        _testBigDecimals(f, input, data, 3, 1);
    }

    private void _testBigDecimals(JsonFactory f, BigDecimal[] values,
            byte[] data, int offset, int readSize) throws IOException
    {
        AsyncReaderWrapper r = asyncForBytes(f, readSize, data, offset);
        // start with "no token"
        assertNull(r.currentToken());
        assertToken(JsonToken.START_ARRAY, r.nextToken());
        for (int i = 0; i < values.length; ++i) {
            BigDecimal expValue = values[i];
            assertToken(JsonToken.VALUE_NUMBER_FLOAT, r.nextToken());
            assertEquals(expValue, r.getBigDecimalValue());
            assertEquals(NumberType.BIG_DECIMAL, r.getNumberType());
        }
        assertToken(JsonToken.END_ARRAY, r.nextToken());
        assertNull(r.nextToken());
        assertTrue(r.isClosed());
    }
*/
}

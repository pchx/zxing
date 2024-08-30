/*
 * Copyright 2018 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.zxing.common.reedsolomon;

import org.junit.Assert;
import org.junit.Test;

/** Tests {@link GenericGFPoly}. */
public final class GenericGFPolyTestCase extends Assert {

  private static final GenericGF FIELD = GenericGF.QR_CODE_FIELD_256;

  private GenericGFPoly GetTestPolynomial() {
    return new GenericGFPoly(FIELD, new int[] {0, 1, 2, 3, 4, 5, 6, 7});
  }

  @Test
  public void testPolynomialString() {
    assertEquals("0", FIELD.getZero().toString());
    assertEquals("-1", FIELD.buildMonomial(0, -1).toString());
    GenericGFPoly p = new GenericGFPoly(FIELD, new int[] {3, 0, -2, 1, 1});
    assertEquals("a^25x^4 - ax^2 + x + 1", p.toString());
    p = new GenericGFPoly(FIELD, new int[] {3});
    assertEquals("a^25", p.toString());
  }

  @Test
  public void testSimplify() {
    GenericGFPoly p = new GenericGFPoly(FIELD, new int[] {0, 0, 1, 0, 123});
    assertArrayEquals(p.getCoefficients(), new int[] {1, 0, 123});
  }

  @Test
  public void testZero() {
    assertEquals(FIELD.getZero(), FIELD.buildMonomial(1, 0));
    assertEquals(FIELD.getZero(), FIELD.buildMonomial(1, 2).multiply(0));
  }

  @Test
  public void testEvaluate() {
    assertEquals(3, FIELD.buildMonomial(0, 3).evaluateAt(0));

    GenericGFPoly poly = GetTestPolynomial();
    assertEquals(7, poly.evaluateAt(0));
    assertEquals(0, poly.evaluateAt(1));
    assertEquals(229, poly.evaluateAt(15));
    assertEquals(171, poly.evaluateAt(255));
  }

  @Test
  public void testGetDegree() {
    assertEquals(0, FIELD.getZero().getDegree());
    assertEquals(0, FIELD.getOne().getDegree());
    assertEquals(6, GetTestPolynomial().getDegree());
  }

  @Test
  public void testIsZero() {
    assertTrue(FIELD.getZero().isZero());
    assertFalse(FIELD.getOne().isZero());
    assertFalse(GetTestPolynomial().isZero());
  }

  @Test
  public void testMultiplyScalar() {
    GenericGFPoly poly = GetTestPolynomial();
    GenericGFPoly result = poly.multiply(0);
    assertTrue(result.isZero());

    result = poly.multiply(1);
    assertArrayEquals(result.getCoefficients(), new int[] {1, 2, 3, 4, 5, 6, 7});

    result = poly.multiply(2);
    assertArrayEquals(result.getCoefficients(), new int[] {2, 4, 6, 8, 10, 12, 14});

    result = poly.multiply(255);
    assertArrayEquals(result.getCoefficients(), new int[] {255, 227, 28, 219, 36, 56, 199});
  }

  @Test
  public void testMultiply() {
    GenericGFPoly poly = GetTestPolynomial();
    GenericGFPoly zero = FIELD.getZero();

    assertTrue(poly.multiply(zero).isZero());
    assertTrue(zero.multiply(poly).isZero());

    GenericGFPoly result = poly.multiply(poly);
    assertArrayEquals(result.getCoefficients(), new int[] {1, 0, 4, 0, 5, 0, 16, 0, 17, 0, 20, 0, 21});

    GenericGFPoly a = new GenericGFPoly(FIELD, new int[] {1, 16, 0});
    GenericGFPoly b = new GenericGFPoly(FIELD, new int[] {2, 128});
    result = b.multiply(a);
    assertArrayEquals(result.getCoefficients(), new int[] {2, 160, 232, 0});
  }

  @Test
  public void testMultiplyByMonomial() {
    GenericGFPoly poly = GetTestPolynomial();

    GenericGFPoly result = poly.multiplyByMonomial(5, 0);
    assertTrue(result.isZero());

    result = poly.multiplyByMonomial(2, 13);
    assertArrayEquals(result.getCoefficients(), new int[] {13, 26, 23, 52, 57, 46, 35, 0, 0});

    result = poly.multiplyByMonomial(0, 1);
    assertArrayEquals(result.getCoefficients(), new int[] {1, 2, 3, 4, 5, 6, 7});
  }

  @Test
  public void testDivide() {
    GenericGFPoly dividend = new GenericGFPoly(FIELD, new int[] {0x12, 0x34, 0x56, 0, 0, 0, 0});
    GenericGFPoly divisor = new GenericGFPoly(FIELD, new int[] {0x01, 0x0f, 0x36, 0x78, 0x40});
    GenericGFPoly remainder = FIELD.getOne();

    // divide() returns an array of [quotient, remainder].
    GenericGFPoly[] result = dividend.divide(divisor);
    assertArrayEquals(result[0].getCoefficients(), new int[] {0x12, 0xda, 0xdf});
    assertArrayEquals(result[1].getCoefficients(), new int[] {0x37, 0xe6, 0x78, 0xd9});

    // Test some basic identities; x divided by x is 1, remainder 0.
    result = dividend.divide(dividend);
    assertArrayEquals(result[0].getCoefficients(), new int[] {1});
    assertArrayEquals(result[1].getCoefficients(), new int[] {0});

    // Likewise, x divided by 1 is x, remainder 0.
    result = dividend.divide(FIELD.getOne());
    assertArrayEquals(result[0].getCoefficients(), dividend.getCoefficients());
    assertArrayEquals(result[1].getCoefficients(), new int[] {0});

    // Next, test a divisor of larger degree than the dividend.
    result = divisor.divide(dividend);
    assertArrayEquals(result[0].getCoefficients(), new int[] {0});
    assertArrayEquals(result[1].getCoefficients(), divisor.getCoefficients());
  }
}

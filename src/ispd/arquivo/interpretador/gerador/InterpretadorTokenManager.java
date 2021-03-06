/* Generated By:JavaCC: Do not edit this line. InterpretadorTokenManager.java */
/* ==========================================================
 * iSPD : iconic Simulator of Parallel and Distributed System
 * ==========================================================
 *
 * (C) Copyright 2010-2014, by Grupo de pesquisas em Sistemas Paralelos e Distribuídos da Unesp (GSPD).
 *
 * Project Info:  http://gspd.dcce.ibilce.unesp.br/
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * [Oracle and Java are registered trademarks of Oracle and/or its affiliates. 
 * Other names may be trademarks of their respective owners.]
 *
 * (C) Copyright 2014, by Grupo de pesquisas em Sistemas Paralelos e Distribuídos da Unesp (GSPD).
 *
 * Original Author:  Denison Menezes (for GSPD);
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * 
 * 09-Set-2014 : Version 2.0;
 *
 */
package ispd.arquivo.interpretador.gerador;
import javax.swing.JOptionPane;

/** Token Manager. */
public class InterpretadorTokenManager implements InterpretadorConstants
{
private int contaErrosLex = 0;
private String erros = new String("");


public int encontrouErroLex()
{
        return contaErrosLex;
}

public void addErro(String msg)
{
        erros = erros+"\u005cn"+msg;
}

public String getErros()
{
        return erros;
}

  /** Debug output. */
  public  java.io.PrintStream debugStream = System.out;
  /** Set debug output. */
  public  void setDebugStream(java.io.PrintStream ds) { debugStream = ds; }
private final int jjStopStringLiteralDfa_0(int pos, long active0)
{
   switch (pos)
   {
      case 0:
         if ((active0 & 0xc0040000000L) != 0L)
            return 2;
         if ((active0 & 0x3fffeL) != 0L)
         {
            jjmatchedKind = 38;
            return 11;
         }
         return -1;
      case 1:
         if ((active0 & 0x3fffeL) != 0L)
         {
            jjmatchedKind = 38;
            jjmatchedPos = 1;
            return 11;
         }
         return -1;
      case 2:
         if ((active0 & 0x3fffeL) != 0L)
         {
            jjmatchedKind = 38;
            jjmatchedPos = 2;
            return 11;
         }
         return -1;
      case 3:
         if ((active0 & 0xf6eeL) != 0L)
         {
            if (jjmatchedPos != 3)
            {
               jjmatchedKind = 38;
               jjmatchedPos = 3;
            }
            return 11;
         }
         if ((active0 & 0x30910L) != 0L)
            return 11;
         return -1;
      case 4:
         if ((active0 & 0x20L) != 0L)
            return 11;
         if ((active0 & 0x1f6ceL) != 0L)
         {
            jjmatchedKind = 38;
            jjmatchedPos = 4;
            return 11;
         }
         return -1;
      case 5:
         if ((active0 & 0x404L) != 0L)
            return 11;
         if ((active0 & 0x1f2caL) != 0L)
         {
            jjmatchedKind = 38;
            jjmatchedPos = 5;
            return 11;
         }
         return -1;
      case 6:
         if ((active0 & 0xf2c2L) != 0L)
         {
            jjmatchedKind = 38;
            jjmatchedPos = 6;
            return 11;
         }
         if ((active0 & 0x10008L) != 0L)
            return 11;
         return -1;
      case 7:
         if ((active0 & 0xd240L) != 0L)
            return 11;
         if ((active0 & 0x2082L) != 0L)
         {
            jjmatchedKind = 38;
            jjmatchedPos = 7;
            return 11;
         }
         return -1;
      case 8:
         if ((active0 & 0x2000L) != 0L)
         {
            jjmatchedKind = 38;
            jjmatchedPos = 8;
            return 11;
         }
         if ((active0 & 0x82L) != 0L)
            return 11;
         return -1;
      default :
         return -1;
   }
}
private final int jjStartNfa_0(int pos, long active0)
{
   return jjMoveNfa_0(jjStopStringLiteralDfa_0(pos, active0), pos + 1);
}
private int jjStopAtPos(int pos, int kind)
{
   jjmatchedKind = kind;
   jjmatchedPos = pos;
   return pos + 1;
}
private int jjMoveStringLiteralDfa0_0()
{
   switch(curChar)
   {
      case 12:
         return jjStartNfaWithStates_0(0, 43, 2);
      case 13:
         return jjStartNfaWithStates_0(0, 42, 2);
      case 40:
         return jjStopAtPos(0, 34);
      case 41:
         return jjStopAtPos(0, 35);
      case 42:
         return jjStartNfaWithStates_0(0, 30, 2);
      case 43:
         return jjStopAtPos(0, 33);
      case 45:
         return jjStopAtPos(0, 32);
      case 47:
         jjmatchedKind = 31;
         return jjMoveStringLiteralDfa1_0(0x3000000000000L);
      case 58:
         return jjStopAtPos(0, 51);
      case 67:
         return jjMoveStringLiteralDfa1_0(0x1080L);
      case 68:
         return jjMoveStringLiteralDfa1_0(0x2048L);
      case 69:
         return jjMoveStringLiteralDfa1_0(0x20L);
      case 70:
         return jjMoveStringLiteralDfa1_0(0x800L);
      case 73:
         return jjMoveStringLiteralDfa1_0(0x200L);
      case 82:
         return jjMoveStringLiteralDfa1_0(0xc400L);
      case 83:
         return jjMoveStringLiteralDfa1_0(0x6L);
      case 84:
         return jjMoveStringLiteralDfa1_0(0x10110L);
      case 85:
         return jjMoveStringLiteralDfa1_0(0x20000L);
      case 91:
         jjmatchedKind = 52;
         return jjMoveStringLiteralDfa1_0(0x3ffc0000L);
      case 93:
         return jjStopAtPos(0, 53);
      default :
         return jjMoveNfa_0(3, 0);
   }
}
private int jjMoveStringLiteralDfa1_0(long active0)
{
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(0, active0);
      return 1;
   }
   switch(curChar)
   {
      case 42:
         if ((active0 & 0x1000000000000L) != 0L)
            return jjStopAtPos(1, 48);
         break;
      case 47:
         if ((active0 & 0x2000000000000L) != 0L)
            return jjStopAtPos(1, 49);
         break;
      case 65:
         return jjMoveStringLiteralDfa2_0(active0, 0x10410L);
      case 67:
         return jjMoveStringLiteralDfa2_0(active0, 0x2L);
      case 69:
         return jjMoveStringLiteralDfa2_0(active0, 0xe000L);
      case 73:
         return jjMoveStringLiteralDfa2_0(active0, 0x940L);
      case 76:
         return jjMoveStringLiteralDfa2_0(active0, 0x2000000L);
      case 77:
         return jjMoveStringLiteralDfa2_0(active0, 0x20000000L);
      case 78:
         return jjMoveStringLiteralDfa2_0(active0, 0x10300220L);
      case 79:
         return jjMoveStringLiteralDfa2_0(active0, 0x80L);
      case 80:
         return jjMoveStringLiteralDfa2_0(active0, 0x1400000L);
      case 82:
         return jjMoveStringLiteralDfa2_0(active0, 0x1000L);
      case 83:
         return jjMoveStringLiteralDfa2_0(active0, 0x20000L);
      case 84:
         return jjMoveStringLiteralDfa2_0(active0, 0xc8c0004L);
      case 89:
         return jjMoveStringLiteralDfa2_0(active0, 0x8L);
      default :
         break;
   }
   return jjStartNfa_0(0, active0);
}
private int jjMoveStringLiteralDfa2_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(0, old0);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(1, active0);
      return 2;
   }
   switch(curChar)
   {
      case 65:
         return jjMoveStringLiteralDfa3_0(active0, 0x4L);
      case 67:
         return jjMoveStringLiteralDfa3_0(active0, 0xecc2000L);
      case 69:
         return jjMoveStringLiteralDfa3_0(active0, 0x21000L);
      case 70:
         return jjMoveStringLiteralDfa3_0(active0, 0x20000800L);
      case 72:
         return jjMoveStringLiteralDfa3_0(active0, 0x2L);
      case 77:
         return jjMoveStringLiteralDfa3_0(active0, 0x180L);
      case 78:
         return jjMoveStringLiteralDfa3_0(active0, 0x408L);
      case 80:
         return jjMoveStringLiteralDfa3_0(active0, 0x1000000L);
      case 83:
         return jjMoveStringLiteralDfa3_0(active0, 0x1c050L);
      case 84:
         return jjMoveStringLiteralDfa3_0(active0, 0x10300220L);
      default :
         break;
   }
   return jjStartNfa_0(1, active0);
}
private int jjMoveStringLiteralDfa3_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(1, old0);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(2, active0);
      return 3;
   }
   switch(curChar)
   {
      case 65:
         return jjMoveStringLiteralDfa4_0(active0, 0x8L);
      case 67:
         return jjMoveStringLiteralDfa4_0(active0, 0x200000L);
      case 68:
         return jjMoveStringLiteralDfa4_0(active0, 0x400L);
      case 69:
         if ((active0 & 0x100L) != 0L)
            return jjStartNfaWithStates_0(3, 8, 11);
         return jjMoveStringLiteralDfa4_0(active0, 0x30000202L);
      case 75:
         if ((active0 & 0x10L) != 0L)
         {
            jjmatchedKind = 4;
            jjmatchedPos = 3;
         }
         return jjMoveStringLiteralDfa4_0(active0, 0x10000L);
      case 77:
         return jjMoveStringLiteralDfa4_0(active0, 0x8000000L);
      case 79:
         if ((active0 & 0x800L) != 0L)
            return jjStartNfaWithStates_0(3, 11, 11);
         return jjMoveStringLiteralDfa4_0(active0, 0x4000L);
      case 80:
         return jjMoveStringLiteralDfa4_0(active0, 0x400c0L);
      case 82:
         if ((active0 & 0x20000L) != 0L)
            return jjStartNfaWithStates_0(3, 17, 11);
         return jjMoveStringLiteralDfa4_0(active0, 0x802020L);
      case 83:
         return jjMoveStringLiteralDfa4_0(active0, 0x101000L);
      case 84:
         return jjMoveStringLiteralDfa4_0(active0, 0x4008004L);
      case 85:
         return jjMoveStringLiteralDfa4_0(active0, 0x400000L);
      case 93:
         if ((active0 & 0x80000L) != 0L)
            return jjStopAtPos(3, 19);
         else if ((active0 & 0x1000000L) != 0L)
            return jjStopAtPos(3, 24);
         else if ((active0 & 0x2000000L) != 0L)
            return jjStopAtPos(3, 25);
         break;
      default :
         break;
   }
   return jjStartNfa_0(2, active0);
}
private int jjMoveStringLiteralDfa4_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(2, old0);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(3, active0);
      return 4;
   }
   switch(curChar)
   {
      case 65:
         return jjMoveStringLiteralDfa5_0(active0, 0x40L);
      case 67:
         return jjMoveStringLiteralDfa5_0(active0, 0x1000L);
      case 68:
         return jjMoveStringLiteralDfa5_0(active0, 0x2L);
      case 69:
         return jjMoveStringLiteralDfa5_0(active0, 0x2000L);
      case 73:
         return jjMoveStringLiteralDfa5_0(active0, 0x4L);
      case 76:
         return jjMoveStringLiteralDfa5_0(active0, 0x80L);
      case 77:
         return jjMoveStringLiteralDfa5_0(active0, 0x8L);
      case 79:
         return jjMoveStringLiteralDfa5_0(active0, 0x400L);
      case 80:
         return jjMoveStringLiteralDfa5_0(active0, 0x10000L);
      case 82:
         return jjMoveStringLiteralDfa5_0(active0, 0x8200L);
      case 84:
         return jjMoveStringLiteralDfa5_0(active0, 0x8000000L);
      case 85:
         return jjMoveStringLiteralDfa5_0(active0, 0x4000L);
      case 89:
         if ((active0 & 0x20L) != 0L)
            return jjStartNfaWithStates_0(4, 5, 11);
         break;
      case 93:
         if ((active0 & 0x40000L) != 0L)
            return jjStopAtPos(4, 18);
         else if ((active0 & 0x100000L) != 0L)
            return jjStopAtPos(4, 20);
         else if ((active0 & 0x200000L) != 0L)
            return jjStopAtPos(4, 21);
         else if ((active0 & 0x400000L) != 0L)
            return jjStopAtPos(4, 22);
         else if ((active0 & 0x800000L) != 0L)
            return jjStopAtPos(4, 23);
         else if ((active0 & 0x4000000L) != 0L)
            return jjStopAtPos(4, 26);
         else if ((active0 & 0x10000000L) != 0L)
            return jjStopAtPos(4, 28);
         else if ((active0 & 0x20000000L) != 0L)
            return jjStopAtPos(4, 29);
         break;
      default :
         break;
   }
   return jjStartNfa_0(3, active0);
}
private int jjMoveStringLiteralDfa5_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(3, old0);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(4, active0);
      return 5;
   }
   switch(curChar)
   {
      case 65:
         return jjMoveStringLiteralDfa6_0(active0, 0x2000L);
      case 67:
         if ((active0 & 0x4L) != 0L)
            return jjStartNfaWithStates_0(5, 2, 11);
         return jjMoveStringLiteralDfa6_0(active0, 0x40L);
      case 69:
         return jjMoveStringLiteralDfa6_0(active0, 0x11080L);
      case 73:
         return jjMoveStringLiteralDfa6_0(active0, 0x8008L);
      case 77:
         if ((active0 & 0x400L) != 0L)
            return jjStartNfaWithStates_0(5, 10, 11);
         break;
      case 82:
         return jjMoveStringLiteralDfa6_0(active0, 0x4000L);
      case 85:
         return jjMoveStringLiteralDfa6_0(active0, 0x2L);
      case 86:
         return jjMoveStringLiteralDfa6_0(active0, 0x200L);
      case 93:
         if ((active0 & 0x8000000L) != 0L)
            return jjStopAtPos(5, 27);
         break;
      default :
         break;
   }
   return jjStartNfa_0(4, active0);
}
private int jjMoveStringLiteralDfa6_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(4, old0);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(5, active0);
      return 6;
   }
   switch(curChar)
   {
      case 65:
         return jjMoveStringLiteralDfa7_0(active0, 0x200L);
      case 67:
         if ((active0 & 0x8L) != 0L)
            return jjStartNfaWithStates_0(6, 3, 11);
         return jjMoveStringLiteralDfa7_0(active0, 0xc000L);
      case 76:
         return jjMoveStringLiteralDfa7_0(active0, 0x2L);
      case 78:
         return jjMoveStringLiteralDfa7_0(active0, 0x1000L);
      case 82:
         if ((active0 & 0x10000L) != 0L)
            return jjStartNfaWithStates_0(6, 16, 11);
         break;
      case 83:
         return jjMoveStringLiteralDfa7_0(active0, 0x2000L);
      case 84:
         return jjMoveStringLiteralDfa7_0(active0, 0xc0L);
      default :
         break;
   }
   return jjStartNfa_0(5, active0);
}
private int jjMoveStringLiteralDfa7_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(5, old0);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(6, active0);
      return 7;
   }
   switch(curChar)
   {
      case 69:
         if ((active0 & 0x4000L) != 0L)
            return jjStartNfaWithStates_0(7, 14, 11);
         return jjMoveStringLiteralDfa8_0(active0, 0x82L);
      case 72:
         if ((active0 & 0x40L) != 0L)
            return jjStartNfaWithStates_0(7, 6, 11);
         break;
      case 73:
         return jjMoveStringLiteralDfa8_0(active0, 0x2000L);
      case 76:
         if ((active0 & 0x200L) != 0L)
            return jjStartNfaWithStates_0(7, 9, 11);
         break;
      case 84:
         if ((active0 & 0x1000L) != 0L)
            return jjStartNfaWithStates_0(7, 12, 11);
         else if ((active0 & 0x8000L) != 0L)
            return jjStartNfaWithStates_0(7, 15, 11);
         break;
      default :
         break;
   }
   return jjStartNfa_0(6, active0);
}
private int jjMoveStringLiteralDfa8_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(6, old0);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(7, active0);
      return 8;
   }
   switch(curChar)
   {
      case 68:
         if ((active0 & 0x80L) != 0L)
            return jjStartNfaWithStates_0(8, 7, 11);
         break;
      case 78:
         return jjMoveStringLiteralDfa9_0(active0, 0x2000L);
      case 82:
         if ((active0 & 0x2L) != 0L)
            return jjStartNfaWithStates_0(8, 1, 11);
         break;
      default :
         break;
   }
   return jjStartNfa_0(7, active0);
}
private int jjMoveStringLiteralDfa9_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(7, old0);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(8, active0);
      return 9;
   }
   switch(curChar)
   {
      case 71:
         if ((active0 & 0x2000L) != 0L)
            return jjStartNfaWithStates_0(9, 13, 11);
         break;
      default :
         break;
   }
   return jjStartNfa_0(8, active0);
}
private int jjStartNfaWithStates_0(int pos, int kind, int state)
{
   jjmatchedKind = kind;
   jjmatchedPos = pos;
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) { return pos + 1; }
   return jjMoveNfa_0(state, pos + 1);
}
static final long[] jjbitVec0 = {
   0x0L, 0x0L, 0xffffffffffffffffL, 0xffffffffffffffffL
};
private int jjMoveNfa_0(int startState, int curPos)
{
   int startsAt = 0;
   jjnewStateCnt = 11;
   int i = 1;
   jjstateSet[0] = startState;
   int kind = 0x7fffffff;
   for (;;)
   {
      if (++jjround == 0x7fffffff)
         ReInitRounds();
      if (curChar < 64)
      {
         long l = 1L << curChar;
         do
         {
            switch(jjstateSet[--i])
            {
               case 3:
                  if ((0xd0000484fffff8ffL & l) != 0L)
                  {
                     if (kind > 50)
                        kind = 50;
                     jjCheckNAdd(2);
                  }
                  else if ((0x3ff000000000000L & l) != 0L)
                  {
                     if (kind > 36)
                        kind = 36;
                     jjCheckNAddStates(0, 4);
                  }
                  break;
               case 11:
               case 1:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 38)
                     kind = 38;
                  jjCheckNAdd(1);
                  break;
               case 2:
                  if ((0xd0000484fffff8ffL & l) == 0L)
                     break;
                  kind = 50;
                  jjCheckNAdd(2);
                  break;
               case 4:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 36)
                     kind = 36;
                  jjCheckNAdd(4);
                  break;
               case 5:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddTwoStates(5, 6);
                  break;
               case 6:
                  if (curChar == 46)
                     jjCheckNAdd(7);
                  break;
               case 7:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 37)
                     kind = 37;
                  jjCheckNAdd(7);
                  break;
               case 8:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddTwoStates(8, 9);
                  break;
               case 9:
                  if (curChar == 44)
                     jjCheckNAdd(10);
                  break;
               case 10:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 37)
                     kind = 37;
                  jjCheckNAdd(10);
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else if (curChar < 128)
      {
         long l = 1L << (curChar & 077);
         do
         {
            switch(jjstateSet[--i])
            {
               case 3:
                  if ((0x7fffffe07fffffeL & l) != 0L)
                  {
                     if (kind > 38)
                        kind = 38;
                     jjCheckNAddTwoStates(0, 1);
                  }
                  else if ((0x8000000100000000L & l) != 0L)
                  {
                     if (kind > 50)
                        kind = 50;
                     jjCheckNAdd(2);
                  }
                  break;
               case 11:
                  if ((0x7fffffe07fffffeL & l) != 0L)
                  {
                     if (kind > 38)
                        kind = 38;
                     jjCheckNAdd(1);
                  }
                  if ((0x7fffffe07fffffeL & l) != 0L)
                  {
                     if (kind > 38)
                        kind = 38;
                     jjCheckNAddTwoStates(0, 1);
                  }
                  break;
               case 0:
                  if ((0x7fffffe07fffffeL & l) == 0L)
                     break;
                  if (kind > 38)
                     kind = 38;
                  jjCheckNAddTwoStates(0, 1);
                  break;
               case 1:
                  if ((0x7fffffe07fffffeL & l) == 0L)
                     break;
                  if (kind > 38)
                     kind = 38;
                  jjCheckNAdd(1);
                  break;
               case 2:
                  if ((0x8000000100000000L & l) == 0L)
                     break;
                  kind = 50;
                  jjCheckNAdd(2);
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else
      {
         int i2 = (curChar & 0xff) >> 6;
         long l2 = 1L << (curChar & 077);
         do
         {
            switch(jjstateSet[--i])
            {
               case 3:
               case 2:
                  if ((jjbitVec0[i2] & l2) == 0L)
                     break;
                  if (kind > 50)
                     kind = 50;
                  jjCheckNAdd(2);
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      if (kind != 0x7fffffff)
      {
         jjmatchedKind = kind;
         jjmatchedPos = curPos;
         kind = 0x7fffffff;
      }
      ++curPos;
      if ((i = jjnewStateCnt) == (startsAt = 11 - (jjnewStateCnt = startsAt)))
         return curPos;
      try { curChar = input_stream.readChar(); }
      catch(java.io.IOException e) { return curPos; }
   }
}
private int jjMoveStringLiteralDfa0_2()
{
   return jjMoveNfa_2(0, 0);
}
private int jjMoveNfa_2(int startState, int curPos)
{
   int startsAt = 0;
   jjnewStateCnt = 1;
   int i = 1;
   jjstateSet[0] = startState;
   int kind = 0x7fffffff;
   for (;;)
   {
      if (++jjround == 0x7fffffff)
         ReInitRounds();
      if (curChar < 64)
      {
         long l = 1L << curChar;
         do
         {
            switch(jjstateSet[--i])
            {
               case 0:
                  if ((0x2400L & l) != 0L)
                     kind = 46;
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else if (curChar < 128)
      {
         long l = 1L << (curChar & 077);
         do
         {
            switch(jjstateSet[--i])
            {
               default : break;
            }
         } while(i != startsAt);
      }
      else
      {
         int i2 = (curChar & 0xff) >> 6;
         long l2 = 1L << (curChar & 077);
         do
         {
            switch(jjstateSet[--i])
            {
               default : break;
            }
         } while(i != startsAt);
      }
      if (kind != 0x7fffffff)
      {
         jjmatchedKind = kind;
         jjmatchedPos = curPos;
         kind = 0x7fffffff;
      }
      ++curPos;
      if ((i = jjnewStateCnt) == (startsAt = 1 - (jjnewStateCnt = startsAt)))
         return curPos;
      try { curChar = input_stream.readChar(); }
      catch(java.io.IOException e) { return curPos; }
   }
}
private int jjMoveStringLiteralDfa0_1()
{
   switch(curChar)
   {
      case 42:
         return jjMoveStringLiteralDfa1_1(0x100000000000L);
      default :
         return 1;
   }
}
private int jjMoveStringLiteralDfa1_1(long active0)
{
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      return 1;
   }
   switch(curChar)
   {
      case 47:
         if ((active0 & 0x100000000000L) != 0L)
            return jjStopAtPos(1, 44);
         break;
      default :
         return 2;
   }
   return 2;
}
static final int[] jjnextStates = {
   4, 5, 6, 8, 9, 
};

/** Token literal values. */
public static final String[] jjstrLiteralImages = {
"", "\123\103\110\105\104\125\114\105\122", "\123\124\101\124\111\103", 
"\104\131\116\101\115\111\103", "\124\101\123\113", "\105\116\124\122\131", 
"\104\111\123\120\101\103\124\110", "\103\117\115\120\114\105\124\105\104", "\124\111\115\105", 
"\111\116\124\105\122\126\101\114", "\122\101\116\104\117\115", "\106\111\106\117", 
"\103\122\105\123\103\105\116\124", "\104\105\103\122\105\101\123\111\116\107", 
"\122\105\123\117\125\122\103\105", "\122\105\123\124\122\111\103\124", "\124\101\123\113\120\105\122", 
"\125\123\105\122", "\133\124\103\120\135", "\133\124\103\135", "\133\116\124\123\135", 
"\133\116\124\103\135", "\133\120\103\125\135", "\133\124\103\122\135", "\133\120\120\135", 
"\133\114\103\135", "\133\124\103\124\135", "\133\124\103\115\124\135", "\133\116\124\105\135", 
"\133\115\106\105\135", "\52", "\57", "\55", "\53", "\50", "\51", null, null, null, null, null, null, 
null, null, null, null, null, null, null, null, null, "\72", "\133", "\135", };

/** Lexer state names. */
public static final String[] lexStateNames = {
   "DEFAULT",
   "multilinecoment",
   "singlelinecoment",
};

/** Lex State array. */
public static final int[] jjnewLexState = {
   -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 
   -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 0, -1, 0, -1, 1, 2, 
   -1, -1, -1, -1, 
};
static final long[] jjtoToken = {
   0x38007fffffffffL, 
};
static final long[] jjtoSkip = {
   0x7ff8000000000L, 
};
static final long[] jjtoSpecial = {
   0x4000000000000L, 
};
protected SimpleCharStream input_stream;
private final int[] jjrounds = new int[11];
private final int[] jjstateSet = new int[22];
private final StringBuilder jjimage = new StringBuilder();
private StringBuilder image = jjimage;
private int jjimageLen;
private int lengthOfMatch;
protected char curChar;
/** Constructor. */
public InterpretadorTokenManager(SimpleCharStream stream){
   if (SimpleCharStream.staticFlag)
      throw new Error("ERROR: Cannot use a static CharStream class with a non-static lexical analyzer.");
   input_stream = stream;
}

/** Constructor. */
public InterpretadorTokenManager(SimpleCharStream stream, int lexState){
   this(stream);
   SwitchTo(lexState);
}

/** Reinitialise parser. */
public void ReInit(SimpleCharStream stream)
{
   jjmatchedPos = jjnewStateCnt = 0;
   curLexState = defaultLexState;
   input_stream = stream;
   ReInitRounds();
}
private void ReInitRounds()
{
   int i;
   jjround = 0x80000001;
   for (i = 11; i-- > 0;)
      jjrounds[i] = 0x80000000;
}

/** Reinitialise parser. */
public void ReInit(SimpleCharStream stream, int lexState)
{
   ReInit(stream);
   SwitchTo(lexState);
}

/** Switch to specified lex state. */
public void SwitchTo(int lexState)
{
   if (lexState >= 3 || lexState < 0)
      throw new TokenMgrError("Error: Ignoring invalid lexical state : " + lexState + ". State unchanged.", TokenMgrError.INVALID_LEXICAL_STATE);
   else
      curLexState = lexState;
}

protected Token jjFillToken()
{
   final Token t;
   final String curTokenImage;
   final int beginLine;
   final int endLine;
   final int beginColumn;
   final int endColumn;
   String im = jjstrLiteralImages[jjmatchedKind];
   curTokenImage = (im == null) ? input_stream.GetImage() : im;
   beginLine = input_stream.getBeginLine();
   beginColumn = input_stream.getBeginColumn();
   endLine = input_stream.getEndLine();
   endColumn = input_stream.getEndColumn();
   t = Token.newToken(jjmatchedKind, curTokenImage);

   t.beginLine = beginLine;
   t.endLine = endLine;
   t.beginColumn = beginColumn;
   t.endColumn = endColumn;

   return t;
}

int curLexState = 0;
int defaultLexState = 0;
int jjnewStateCnt;
int jjround;
int jjmatchedPos;
int jjmatchedKind;

/** Get the next Token. */
public Token getNextToken() 
{
  Token specialToken = null;
  Token matchedToken;
  int curPos = 0;

  EOFLoop :
  for (;;)
  {
   try
   {
      curChar = input_stream.BeginToken();
   }
   catch(java.io.IOException e)
   {
      jjmatchedKind = 0;
      matchedToken = jjFillToken();
      matchedToken.specialToken = specialToken;
      return matchedToken;
   }
   image = jjimage;
   image.setLength(0);
   jjimageLen = 0;

   switch(curLexState)
   {
     case 0:
       try { input_stream.backup(0);
          while (curChar <= 32 && (0x100000600L & (1L << curChar)) != 0L)
             curChar = input_stream.BeginToken();
       }
       catch (java.io.IOException e1) { continue EOFLoop; }
       jjmatchedKind = 0x7fffffff;
       jjmatchedPos = 0;
       curPos = jjMoveStringLiteralDfa0_0();
       break;
     case 1:
       jjmatchedKind = 0x7fffffff;
       jjmatchedPos = 0;
       curPos = jjMoveStringLiteralDfa0_1();
       if (jjmatchedPos == 0 && jjmatchedKind > 45)
       {
          jjmatchedKind = 45;
       }
       break;
     case 2:
       jjmatchedKind = 0x7fffffff;
       jjmatchedPos = 0;
       curPos = jjMoveStringLiteralDfa0_2();
       if (jjmatchedPos == 0 && jjmatchedKind > 47)
       {
          jjmatchedKind = 47;
       }
       break;
   }
     if (jjmatchedKind != 0x7fffffff)
     {
        if (jjmatchedPos + 1 < curPos)
           input_stream.backup(curPos - jjmatchedPos - 1);
        if ((jjtoToken[jjmatchedKind >> 6] & (1L << (jjmatchedKind & 077))) != 0L)
        {
           matchedToken = jjFillToken();
           matchedToken.specialToken = specialToken;
       if (jjnewLexState[jjmatchedKind] != -1)
         curLexState = jjnewLexState[jjmatchedKind];
           return matchedToken;
        }
        else
        {
           if ((jjtoSpecial[jjmatchedKind >> 6] & (1L << (jjmatchedKind & 077))) != 0L)
           {
              matchedToken = jjFillToken();
              if (specialToken == null)
                 specialToken = matchedToken;
              else
              {
                 matchedToken.specialToken = specialToken;
                 specialToken = (specialToken.next = matchedToken);
              }
              SkipLexicalActions(matchedToken);
           }
           else
              SkipLexicalActions(null);
         if (jjnewLexState[jjmatchedKind] != -1)
           curLexState = jjnewLexState[jjmatchedKind];
           continue EOFLoop;
        }
     }
     int error_line = input_stream.getEndLine();
     int error_column = input_stream.getEndColumn();
     String error_after = null;
     boolean EOFSeen = false;
     try { input_stream.readChar(); input_stream.backup(1); }
     catch (java.io.IOException e1) {
        EOFSeen = true;
        error_after = curPos <= 1 ? "" : input_stream.GetImage();
        if (curChar == '\n' || curChar == '\r') {
           error_line++;
           error_column = 0;
        }
        else
           error_column++;
     }
     if (!EOFSeen) {
        input_stream.backup(1);
        error_after = curPos <= 1 ? "" : input_stream.GetImage();
     }
     throw new TokenMgrError(EOFSeen, curLexState, error_line, error_column, error_after, curChar, TokenMgrError.LEXICAL_ERROR);
  }
}

void SkipLexicalActions(Token matchedToken)
{
   switch(jjmatchedKind)
   {
      case 50 :
         image.append(input_stream.GetSuffix(jjimageLen + (lengthOfMatch = jjmatchedPos + 1)));
                addErro("Erro na linha "+input_stream.getEndLine()+". Caracter "+image+" n\u00e3o \u00e9 aceito.");
                contaErrosLex++;
         break;
      default :
         break;
   }
}
private void jjCheckNAdd(int state)
{
   if (jjrounds[state] != jjround)
   {
      jjstateSet[jjnewStateCnt++] = state;
      jjrounds[state] = jjround;
   }
}
private void jjAddStates(int start, int end)
{
   do {
      jjstateSet[jjnewStateCnt++] = jjnextStates[start];
   } while (start++ != end);
}
private void jjCheckNAddTwoStates(int state1, int state2)
{
   jjCheckNAdd(state1);
   jjCheckNAdd(state2);
}

private void jjCheckNAddStates(int start, int end)
{
   do {
      jjCheckNAdd(jjnextStates[start]);
   } while (start++ != end);
}

}

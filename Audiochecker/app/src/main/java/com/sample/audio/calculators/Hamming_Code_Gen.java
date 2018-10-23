package com.sample.audio.calculators;// Hamming Code
import java.util.*;

class Hamming_Code_Gen
{
	static Double[][]  generateMatrix (String msg){ // Generates a Hamming code according to the number of data bits
		int r=0,m=msg.length(); 
		//calculate number of parity bits needed using m+r+1<=2^r
		while(true)
		{
			if(m+r+1<=Math.pow(2,r))
			{
				break;
			}
			r++;
		}
		// System.out.println("Number of parity bits needed : "+r);
		int transLength = msg.length()+r,temp=0,temp2=0,j=0;
		double transMsg[]=new double[transLength+1]; //+1 because starts with 1
		for(int i=1;i<=transLength;i++)
		{
			temp2=(int)Math.pow(2,temp);
			if(i%temp2!=0)
			{
				transMsg[i]=Double.parseDouble(Character.toString(msg.charAt(j)));
				j++;
			}
			else
			{
				temp++;
			}
		}
		
		// for(int i=1;i<=transLength;i++)
		// {
		// 	System.out.print(transMsg[i]);
		// }
		// System.out.println();	

		for(int i=0;i<r;i++)
		{
			int smallStep=(int)Math.pow(2,i);
			int bigStep=smallStep*2;
			int start=smallStep,checkPos=start;
			// System.out.println("Calculating Parity bit for Position : "+smallStep);
			// System.out.print("Bits to be checked : ");
			while(true)
			{
				for(int k=start;k<=start+smallStep-1;k++)
				{
					checkPos=k;
					// System.out.print(checkPos+" ");
					if(k>transLength)
					{
						break;
					}
					transMsg[smallStep]= Double.longBitsToDouble(Double.doubleToRawLongBits(transMsg[smallStep]) ^ Double.doubleToRawLongBits(transMsg[checkPos]));

				}
				if(checkPos>transLength)
				{
					break;
				}
				else
				{
					start=start+bigStep;
				}
			}
			// System.out.println();
		}	
		//Display encoded message
		System.out.print("Hamming Encoded Message : ");
		for(int i=1;i<=transLength;i++)
		{
            System.out.print(transMsg[i]+" ");
        }
        
        System.out.println();

        Double[][] matrix = {{0.00}, {0.00}, {0.00}, {0.00}, {0.00}, {0.00}, {0.00}, {0.00}, {0.00}, {0.00}, {0.00}, {0.00}, {0.00}, {0.00}, {0.00}};

        for (int i=1;i <= transLength; i++){
            matrix[i-1][0] = transMsg[i];
            //  System.arraycopy(transMsg, (i*transLength), matrix[i], 0, transLength);
        }
        // System.out.println("\n\n"+Integer.toString(matrix.length)+" Here: "+Arrays.toString(matrix));
        return matrix;
    }

    public static Double[][] checkmatrix (Double[][] A){ // for Hamming (15,11)
        Double[][] output = { { 0.00}, {0.00}, {0.00}, {0.00 } };
        output[0][0] = Double.longBitsToDouble(Double.doubleToRawLongBits(A[0][0]) ^ Double.doubleToRawLongBits(A[14][0]) ^ Double.doubleToRawLongBits(A[12][0]) ^ Double.doubleToRawLongBits(A[10][0]) ^ Double.doubleToRawLongBits(A[8][0]) ^ Double.doubleToRawLongBits(A[6][0]) ^ Double.doubleToRawLongBits(A[4][0]) ^ Double.doubleToRawLongBits(A[2][0]));
        output[1][0] = Double.longBitsToDouble(Double.doubleToRawLongBits(A[1][0]) ^ Double.doubleToRawLongBits(A[14][0]) ^ Double.doubleToRawLongBits(A[13][0]) ^ Double.doubleToRawLongBits(A[10][0]) ^ Double.doubleToRawLongBits(A[9][0]) ^ Double.doubleToRawLongBits(A[6][0]) ^ Double.doubleToRawLongBits(A[5][0]) ^ Double.doubleToRawLongBits(A[2][0]));
        output[2][0] = Double.longBitsToDouble(Double.doubleToRawLongBits(A[3][0]) ^ Double.doubleToRawLongBits(A[14][0]) ^ Double.doubleToRawLongBits(A[13][0]) ^ Double.doubleToRawLongBits(A[12][0]) ^ Double.doubleToRawLongBits(A[11][0]) ^ Double.doubleToRawLongBits(A[6][0]) ^ Double.doubleToRawLongBits(A[5][0]) ^ Double.doubleToRawLongBits(A[4][0]));
        output[3][0] = Double.longBitsToDouble(Double.doubleToRawLongBits(A[7][0]) ^ Double.doubleToRawLongBits(A[14][0]) ^ Double.doubleToRawLongBits(A[13][0]) ^ Double.doubleToRawLongBits(A[12][0]) ^ Double.doubleToRawLongBits(A[11][0]) ^ Double.doubleToRawLongBits(A[10][0]) ^ Double.doubleToRawLongBits(A[9][0]) ^ Double.doubleToRawLongBits(A[8][0]));
        return output;
    }
    
    public static Double[][] multiplicar(Double[][] A, Double[][] B) { //multiplica e tira o %2

        int aRows = A.length;
        int aColumns = A[0].length;
        int bRows = B.length;
        int bColumns = B[0].length;

        // System.out.println(aRows+" "+aColumns+" "+bRows+" "+bColumns);

        if (aColumns != bRows) {
            throw new IllegalArgumentException("A:Rows: " + aColumns + " did not match B:Columns " + bRows + ".");
        }

        Double[][] C = new Double[aRows][bColumns];
        for (int i = 0; i < aRows; i++) {
            for (int j = 0; j < bColumns; j++) {
                C[i][j] = 0.00000;
            }
        }

        for (int i = 0; i < aRows; i++) { // aRow
            for (int j = 0; j < bColumns; j++) { // bColumn
                for (int k = 0; k < aColumns; k++) { // aColumn
                    C[i][j] += A[i][k] * B[k][j];
                }
                C[i][j] = C[i][j] % 2;  // tira % 2 do valor
            }   
        }

        return C;
    }

    public static void printMatrix(Double[][] A) {
        System.out.printf("Matrix %dx%d\n",A.length,A[0].length);
        for (int i = 0; i < A.length; i++) {
            for (int j = 0; j < A[0].length; j++)
                System.out.print(A[i][j] + " ");
            System.out.println();
        }
    }

    public static Double[][] cloneMatrix (Double[][] mat) {
        Double [][] newMat = new Double[mat.length][];
        for(int i = 0; i < mat.length; i++)
            newMat[i] = mat[i].clone();
        return newMat;
    }

    public static String doubleToIntMatrix (Double[][] matrix){
        String strMat = "";
        for (Double[] mat:matrix) {
            strMat += Integer.toString((int) ((double) (mat[0])));
        }
        return strMat;
    }

    public static String doubleToIntMatrix (Double[] matrix){
        String strMat = "";
        for (Double mat:matrix) {
            strMat += Integer.toString((int) ((double) (mat)));
        }
        return strMat;
    }
    
}
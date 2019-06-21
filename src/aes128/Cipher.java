package aes128;

import static aes128.S_box.*;

public class Cipher {
	
	private static byte[][] Rcon = {{0x00, 0x00, 0x00, 0x00},
									{0x01, 0x00, 0x00, 0x00},
									{0x02, 0x00, 0x00, 0x00},
									{0x04, 0x00, 0x00, 0x00},
									{0x08, 0x00, 0x00, 0x00},
									{0x10, 0x00, 0x00, 0x00},
									{0x20, 0x00, 0x00, 0x00},
									{0x40, 0x00, 0x00, 0x00},
									{(byte) 0x80, 0x00, 0x00, 0x00},
									{(byte) 0x1b, 0x00, 0x00, 0x00},
									{(byte) 0x36, 0x00, 0x00, 0x00}};
	
	public int Nb ;
	public int Nk ;
	public int Nr ;
	private byte[][] w;
	private int[] s_box_one_dime ;
	private S_box sboxgenerator ;
	
	public Cipher() {
		// TODO Auto-generated constructor stub
		Nb = 4;
		Nk = 4;
		Nr = 10;
		w = new byte[Nb*(Nr+1)][Nb];
		sboxgenerator = new S_box();
		s_box_one_dime = sboxgenerator.generate_one_dime();
	}
	
	public byte[][] getKeyExpansionresult() {
		return w;
	}
	
	public byte[][] encryptstate(byte[][] state) {
		System.out.println("Begin to encrypt state!!!");
//		System.out.println("input:");
//		printstate(state);
		AddRoundKey(0,state);
		for (int i = 1; i < Nr ; i++) {
//			System.out.println("start of round " + i +":");
//			printstate(state);
			SubBytes(state);
//			System.out.println("after subbytes:");
//			printstate(state);
			
			state = ShiftRows(state);
//			System.out.println("after shiftrows:");
//			printstate(state);
			
			state = MixColumns(state);
//			System.out.println("after mixcolumns:");
//			printstate(state);
			
			AddRoundKey(i,state);
//			System.out.println("after round key:");
//			printstate(state);
		}
//		System.out.println("start of round " + Nr +":");
//		printstate(state);
		SubBytes(state);
//		System.out.println("after subbytes:");
//		printstate(state);
		
		state = ShiftRows(state);
//		System.out.println("after shiftrows:");
//		printstate(state);
		
		AddRoundKey(Nr,state);
		System.out.println("output:");
		
		
		return state;
	}
	
	
	private String statetostring(byte[][] state) {
		StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				stringBuilder.append(String.format("%02X", state[j][i]));
			}
		}
		String string = stringBuilder.toString();
		return string;
	}
	
	
	private void SubBytes(byte[][] state) {
		for (int i = 0; i < Nb; i++) {
			for (int j = 0; j < Nb; j++) {
				state[i][j] = (byte)(s_box_one_dime[state[i][j] & 0x00ff] & 0x00ff);
			}
		}
	}
	
	private byte[][]  ShiftRows(byte[][] state) {
		byte[][] tmp = new byte[Nb][Nb];
		for (int i = 0; i < Nb; i++) {
			for (int j = 0; j < Nb; j++) {
				tmp[i][j] = state[i][(Nb+i+j)%Nb];
			}
		}
		return tmp;
	}
	
	private byte[][]  MixColumns(byte[][] state) {
		byte[] a = {2,3,1,1};
		
		byte[][] tmp = new byte[Nb][Nb];
		for(int j = 0 ; j < Nb ; j++) {
			tmp[0][j] = (byte)(mul(a[0], state[0][j])^mul(a[1], state[1][j])^
								mul(a[2], state[2][j])^mul(a[3], state[3][j]));
			tmp[1][j] = (byte)(mul(a[3], state[0][j])^mul(a[0], state[1][j])^
								mul(a[1], state[2][j])^mul(a[2], state[3][j]));
			tmp[2][j] = (byte)(mul(a[2], state[0][j])^mul(a[3], state[1][j])^
								mul(a[0], state[2][j])^mul(a[1], state[3][j]));
			tmp[3][j] = (byte)(mul(a[1], state[0][j])^mul(a[2], state[1][j])^
								mul(a[3], state[2][j])^mul(a[0], state[3][j]));
		}
		return tmp;
	}
	
	private void AddRoundKey(int currentroundnum,byte[][] state) {
		for (int i = 0; i < 4; i++) {
			state[0][i] = (byte) (state[0][i] ^ w[Nb*currentroundnum + i][0]);
			state[1][i] = (byte) (state[1][i] ^ w[Nb*currentroundnum + i][1]);
			state[2][i] = (byte) (state[2][i] ^ w[Nb*currentroundnum + i][2]);
			state[3][i] = (byte) (state[3][i] ^ w[Nb*currentroundnum + i][3]);
		}
	}
	
	public void Keyexpansion(byte[] key) {
		byte[] temp = new byte[4];
		int i = 0;
		while(i < Nk) {
			w[i][0] = key[4*i];
			w[i][1] = key[4*i + 1];
			w[i][2] = key[4*i + 2];
			w[i][3] = key[4*i + 3];
			i++;
		}
		i = Nk;
		while(i < Nb *(Nr+1)) {
			temp = w[i - 1];
			if(i%Nk == 0) {
				temp = xorbytes(SubWord(RotWord(temp)), Rcon[i/Nk]);
			}
			else if (Nk > 6 && i%Nk == 4) {
				temp = SubWord(RotWord(temp));
			}
			w[i] = xorbytes(w[i-Nk], temp);
			i++;
		}
		return;
	}
	
	private byte[] RotWord(byte[] A) {
		byte[] result = new byte[A.length];
		for (int i = 0; i < A.length; i++) {
			result[i] = A[(i+1)%A.length];
		}
		return result;
	}
	
	private byte[] SubWord(byte[] A) {
		byte[] result = new byte[A.length];
		for (int i = 0; i < A.length; i++) {
			result[i] = (byte)s_box_one_dime[A[i]&0x00ff];
		}
		return result;
	}
	
	private byte[] xorbytes(byte[] A, byte[] B) {
		byte[] result = new byte[A.length];
		for (int i = 0; i < A.length; i++) {
			result[i] = (byte)(A[i]^B[i]);
		}
		return result;
		
	}
	
	public String encryptstringtobytes(String plaintext, String inputkey) {
		StringBuilder stringBuilder = new StringBuilder();
		String sb;
		int start = 0;
		int end = 2;
		if(inputkey.length() != 32) {
			System.out.println("please input correct key!");
			return "";
		}
		byte[] key = new byte[Nk*4];
		//iterate over key two chars a time, add their hex value to matrix
		for (int i = 0; i < key.length; i++) {
				sb = inputkey.substring(start, end);
				key[i] = (byte)(Integer.parseInt(sb.toString(), 16));
				start += 2;
				end += 2;
		}
		
		Keyexpansion(key);
		
		byte[] plaintextbytes = plaintext.getBytes();
		byte[][] state = new byte[Nb][Nb];
		
		for (int i = 0; i < plaintextbytes.length; i++) {
			if(i%16 == 0 && i != 0) {
				stringBuilder.append(encryptstate(state));
				state = new byte[Nb][Nb];
			}
			state[i%4][(i/4)%4] = plaintextbytes[i];	
		}
		stringBuilder.append(encryptstate(state));
		
		return stringBuilder.toString(); 
	}
	
	public byte[][] encryptparsestring(String input, String inputkey) {
		String sb;
		int start = 0;
		int end = 2;
		byte[] key = new byte[Nk*4];
		for (int i = 0; i < key.length; i++) {
			sb = inputkey.substring(start, end);
			key[i] = (byte)(Integer.parseInt(sb.toString(), 16));
			start += 2;
			end += 2;
		}
	
		Keyexpansion(key);
		start = 0;
		end = 2;
		byte[][] state = new byte[Nb][Nb];
		//iterate over line two chars at a time, add their hex value to matrix
		for (int i = 0; i < state.length; i++) {
			for (int j = 0; j < state[0].length; j++) {
				sb = input.substring(start, end);
				state[j][i] = (byte)(Integer.parseInt(sb.toString(), 16));
				start += 2;
				end += 2;
			}
		}
		state = encryptstate(state);
		System.out.println(statetostring(state));
		return state ;
	}
	
	private void printstate(byte[][] state) {
		int i = 0;
		int j = 0;
		for(; i < 4 ; i++) {
			for(j = 0; j < 4 ; j++) {
				String string = String.format("%02X", state[i][j]);
				System.out.print(string+" ");
			}
			System.out.println();
		}
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
	}

}

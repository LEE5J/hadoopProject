package org;

import java.io.*;

public class DataStreams7 {
    // 데이터 타입에 맞게 IO 제공(int double) + strings in a binary type
    public static void main(String[] args) {
        FileOutputStream fos = null;
        DataOutputStream dos = null;

        boolean addTab = false;

        try {
            fos = new FileOutputStream("data.bin");
            dos = new DataOutputStream(fos);
            dos.writeBoolean(false);
            if(addTab) dos.writeChar('\n');
            dos.writeByte((byte)257);
            if(addTab) dos.writeChar('\n');
            dos.writeInt(10);
            if(addTab) dos.writeChar('\n');
            dos.writeDouble(200.5);
            if(addTab) dos.writeChar('\n');

            dos.writeUTF("hello world 한글");
            dos.writeUTF("추석!\n");
            System.out.println("저장하였습니다.");

            FileInputStream fis = null;
            DataInputStream dis = null;

            fis = new FileInputStream("data.bin");
            dis = new DataInputStream(fis);

            boolean boolVar = dis.readBoolean();
            if(addTab) dos.writeChar('\n');
            byte byteVar = dis.readByte();
            if(addTab) dos.writeChar('\n');
            int intVar = dis.readInt();
            if(addTab) dos.writeChar('\n');
            double doubleVar = dis.readDouble();
            if(addTab) dos.writeChar('\n');
            String[] stringVar = new String[3];
            int cnt = 0;
            while(dis.available() > 0)
            {
                stringVar[cnt] = dis.readUTF();
                cnt++;
            }
//            String stringVar = dis.readUTF();
            System.out.println("cnt: " + cnt);
            System.out.println(boolVar);
            System.out.println(byteVar);
            System.out.println(intVar);
            System.out.println(doubleVar);
            for(int i=0; i<cnt; i++)
            {
                System.out.println(stringVar[i]);
            }
//            System.out.println(stringVar[0]);
//            System.out.println(stringVar[1]);

            fis.close();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                dos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

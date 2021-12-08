package com.example.hello;

public class DataReceive {
    float[][][] data = new float[1][256][18];
    int id=0;
    int num=0;
    boolean stats=false;

    public void dataInit(){
        for(int i=0;i<data[0].length;i++){
            for(int j=0;j<data[0][0].length;j++){
                data[0][i][j]=0;
            }
        }
        id=0;
        num=0;
        stats=false;
    }

    public void dataInsert(String str){
        if(!dataCheck(str)) return;

        String[] strlist = str.split(" ");

        for(int i=0;i<strlist.length;i++){
            data[0][id][i] = Float.parseFloat(strlist[i]);
        }

        int c=0;
        for(int i=0;i<14;i++){
            if(data[0][id][i]>0.80) c++;
            //if(data[0][id][i]<0.70) c--;
        }
        if(c>10) num++;
        else num=0;

        id++;

        if(num==10){
            if(id==num){
                dataInit();
            }
            else{
                for(int i=id-num;i<id;i++){
                    for(int j=0;j<data[0][0].length;j++){
                        data[0][i][j]=0;
                    }
                }
                stats=true;
            }
        }

        if(id==data[0].length){
            dataInit();
        }
    }

    public boolean dataCheck(String str){
        String[] strlist = str.split(" ");

        for(int i=0;i<strlist.length;i++){
            if(Float.parseFloat(strlist[i])>1.0||Float.parseFloat(strlist[i])<0.0){
                return false;
            }
        }
        return true;
    }
}

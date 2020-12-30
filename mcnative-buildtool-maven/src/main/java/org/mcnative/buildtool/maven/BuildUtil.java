package org.mcnative.buildtool.maven;

public class BuildUtil {

    public static String transformUUIDtoStringId(String uuid){
        StringBuilder builder = new StringBuilder();
        for (char c : uuid.toCharArray()) {
            if(Character.isDigit(c)){
                char cNew = (char)(Integer.parseInt(String.valueOf(c))+65);
                builder.append(cNew);
            }
        }
        return builder.toString().replace("_","");
    }

}

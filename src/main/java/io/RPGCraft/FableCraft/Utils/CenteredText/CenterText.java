package io.RPGCraft.FableCraft.Utils.CenteredText;

public class CenterText {
  private final static int CENTER_PX = 154;

  public static String centerMessage(String message){
    if(message == null || message.equals("")) return null;

    int messagePxSize = 0;
    boolean previousCode = false;
    boolean isBold = false;

    for(char c : message.toCharArray()){
      if(c == '&'){
        previousCode = true;
        continue;
      }else if(previousCode){
        previousCode = false;
        if(c == 'l' || c == 'L'){
          isBold = true;
          continue;
        }else isBold = false;
      }else{
        DefaultFontInfo dFI = DefaultFontInfo.getDefaultFontInfo(c);
        messagePxSize += isBold ? dFI.getBoldLength() : dFI.getLength();
        messagePxSize++;
      }
    }

    int halvedMessageSize = messagePxSize / 2;
    int toCompensate = CENTER_PX - halvedMessageSize;
    int spaceLength = DefaultFontInfo.SPACE.getLength() + 1;
    int compensated = 0;
    StringBuilder sb = new StringBuilder();
    while(compensated < toCompensate){
      sb.append(" ");
      compensated += spaceLength;
    }

    return sb + message;
  }

  /*public static Component sendCenteredMessage(String msg){
    if(msg == null || msg.equals("")) return null;
    Component message = MM(msg);

    int messagePxSize = 0;
    boolean previousCode = false;
    boolean isBold = false;

    for(char c : message.toCharArray()){
      if(c == '§'){
        previousCode = true;
        continue;
      }else if(previousCode == true){
        previousCode = false;
        if(c == 'l' || c == 'L'){
          isBold = true;
          continue;
        }else isBold = false;
      }else{
        DefaultFontInfo dFI = DefaultFontInfo.getDefaultFontInfo(c);
        messagePxSize += isBold ? dFI.getBoldLength() : dFI.getLength();
        messagePxSize++;
      }
    }

    int halvedMessageSize = messagePxSize / 2;
    int toCompensate = CENTER_PX - halvedMessageSize;
    int spaceLength = DefaultFontInfo.SPACE.getLength() + 1;
    int compensated = 0;
    StringBuilder sb = new StringBuilder();
    while(compensated < toCompensate){
      sb.append(" ");
      compensated += spaceLength;
    }

    return sb + message;
  }*/

}

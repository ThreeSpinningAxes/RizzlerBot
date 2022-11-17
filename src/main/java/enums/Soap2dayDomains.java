package enums;

public enum Soap2dayDomains {
   TO,AC,SH,MX;

   public String getDomainLink() {
       switch (this) {
           case AC -> {
               return "soap2day.ac";
           }
           case TO -> {
               return "soap2day.to";
           }
           case SH -> {
               return "soap2day.sh";
           }
           case MX -> {
               return "soap2day.mx";
           }
           default -> {
               return null;
           }
       }
   }
}

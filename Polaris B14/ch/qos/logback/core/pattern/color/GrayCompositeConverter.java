/*    */ package ch.qos.logback.core.pattern.color;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public class GrayCompositeConverter<E>
/*    */   extends ForegroundCompositeConverterBase<E>
/*    */ {
/*    */   protected String getForegroundColorCode(E event)
/*    */   {
/* 30 */     return "1;30";
/*    */   }
/*    */ }


/* Location:              C:\Users\suzjan\AppData\Roaming\.minecraft\versions\Polaris\Polaris.jar!\ch\qos\logback\core\pattern\color\GrayCompositeConverter.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */
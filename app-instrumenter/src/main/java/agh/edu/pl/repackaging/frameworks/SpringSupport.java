/* (C)2021 */
package agh.edu.pl.repackaging.frameworks;

public class SpringSupport implements FrameworkSupport {
  @Override
  public String getPrefix() {
    return "BOOT-INF/classes/";
  }
}

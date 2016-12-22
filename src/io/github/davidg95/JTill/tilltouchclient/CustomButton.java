/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.JTill.tilltouchclient;

import io.github.davidg95.JTill.jtill.ButtonFunction;
import javax.swing.JButton;

/**
 *
 * @author 1301480
 */
public class CustomButton extends JButton{
    
    private ButtonFunction function;
    
    public CustomButton(){
        super();
    }
    
    public CustomButton(String name, ButtonFunction function){
        super(name);
        this.function = function;
    }

    public ButtonFunction getFunction() {
        return function;
    }

    public void setFunction(ButtonFunction function) {
        this.function = function;
    }
}

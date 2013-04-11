package staff;

import static javax.swing.GroupLayout.Alignment.BASELINE;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.GroupLayout.Group;

/** GitHub event notifier GUI. */
public class HubGUI extends JFrame {
    
    private static final long serialVersionUID = 1L;
    
    /** Create a new notifier GUI. */
    public static void create(final HubPublisher pub) {
        // Create the GUI on the Swing event thread
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new HubGUI(pub).setVisible(true);
            }
        });
    }
    
    private final HubPublisher pub;
    private final GroupLayout layout;
    private final Group labels;
    private final Group widgets;
    private final Group horizontal;
    private final Group vertical;
    
    HubGUI(HubPublisher pub) {
        super("GitHub GUI");
        
        this.pub = pub;
        
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        Container cp = this.getContentPane();
        layout = new GroupLayout(cp);
        cp.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        
        final JLabel typeLabel = new JLabel("Notify me about: ");
        final JTextField typeField = new JTextField(20);
        
        labels = layout.createParallelGroup();
        widgets = layout.createParallelGroup();
        
        labels.addComponent(typeLabel);
        widgets.addComponent(typeField);
        
        horizontal = layout.createSequentialGroup();
        horizontal.addGroup(labels).addGroup(widgets);
        layout.setHorizontalGroup(horizontal);
        
        vertical = layout.createSequentialGroup();
        vertical.addGroup(layout.createParallelGroup(BASELINE)
                .addComponent(typeLabel)
                .addComponent(typeField));
        layout.setVerticalGroup(vertical);
        
        this.pack();
        
        typeField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                subscribe(typeField.getText());
                typeField.setText("");
            }
        });
    }
    
    private void subscribe(String type) {
        JLabel label = new JLabel(type);
        Widget widget;
        try {
            widget = new Widget(type);
        } catch (IllegalArgumentException iae) {
            JOptionPane.showMessageDialog(this, "Invalid event type");
            return;
        }
        
        pub.addListener(widget);
        
        labels.addComponent(label);
        widgets.addComponent(widget.repo);
        vertical.addGroup(layout.createParallelGroup(BASELINE)
                .addComponent(label)
                .addComponent(widget.repo));
        
        // Users might be annoyed that the window keeps changing size, but here goes...
        this.pack();
    }
}

class Widget implements HubListener {
    
    private final HubEvent.Type type;
    public final JLabel repo;
    
    public Widget(String type) {
        this.type = HubEvent.Type.valueOf(type);
        this.repo = new JLabel();
    }
    
    public void event(final HubEvent event) {
        if (event.type == type) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    repo.setText(event.repo);
                }
            });
        }
    }
}

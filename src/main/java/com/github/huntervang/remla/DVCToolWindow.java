package com.github.huntervang.remla;

import static com.github.huntervang.remla.DVCAdd.openDvcAddFilePicker;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;

import java.util.Objects;
import javax.swing.*;
import java.util.Calendar;

public class DVCToolWindow {
    private JButton refreshToolWindowButton;
    private JButton addButton;
    private JLabel currentDate;
    private JLabel currentTime;
    private JLabel timeZone;
    private JPanel dvcToolWindowContent;

    public DVCToolWindow(Project project, ToolWindow toolWindow) {
        refreshToolWindowButton.addActionListener(e -> currentDateTime());
        addButton.addActionListener(e -> openDvcAddFilePicker(project));

        this.currentDateTime();
    }

    public void currentDateTime() {
        // Get current date and time
        Calendar instance = Calendar.getInstance();

        currentDate.setText(
                instance.get(Calendar.DAY_OF_MONTH) + "/"
                        + (instance.get(Calendar.MONTH) + 1) + "/"
                        + instance.get(Calendar.YEAR)
        );
        currentDate.setIcon(new ImageIcon(Objects.requireNonNull(getClass().getResource("/toolWindow/Calendar-icon.png"))));

        int min = instance.get(Calendar.MINUTE);
        String strMin = min < 10 ? "0" + min : String.valueOf(min);
        currentTime.setText(instance.get(Calendar.HOUR_OF_DAY) + ":" + strMin);
        currentTime.setIcon(new ImageIcon(Objects.requireNonNull(getClass().getResource("/toolWindow/Time-icon.png"))));

        // Get time zone
        long gmt_Offset = instance.get(Calendar.ZONE_OFFSET); // offset from GMT in milliseconds
        String str_gmt_Offset = String.valueOf(gmt_Offset / 3600000);
        str_gmt_Offset = (gmt_Offset > 0) ? "GMT + " + str_gmt_Offset : "GMT - " + str_gmt_Offset;
        timeZone.setText(str_gmt_Offset);
        timeZone.setIcon(new ImageIcon(Objects.requireNonNull(getClass().getResource("/toolWindow/Time-zone-icon.png"))));
    }

    public JPanel getContent() {
        return dvcToolWindowContent;
    }

}
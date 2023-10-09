package dev.lpa.todo;

import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.time.LocalDate;

public class TodoItemDialogController {

    @FXML
    private TextField shortDescriptionField;
    @FXML
    private TextArea detailsArea;
    @FXML
    private DatePicker deadlinePicker;

    public ToDoItem processResults() {
        String shortDescription = shortDescriptionField.getText().trim();
        String details = detailsArea.getText().trim();
        LocalDate deadlinevalue = deadlinePicker.getValue();


        ToDoItem newItem =  new ToDoItem(shortDescription, details, deadlinevalue);
        ToDoData.getInstance().addToDoItem(newItem);

        return newItem;
    }
}

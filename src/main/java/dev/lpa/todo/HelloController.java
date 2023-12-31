package dev.lpa.todo;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.util.Callback;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class HelloController {

    private List<ToDoItem> toDoItems;
    @FXML
    private ListView<ToDoItem> todoListView;
    @FXML
    private TextArea itemDetails;
    @FXML
    private Label deadlineLabel;
    @FXML
    private BorderPane mainBorderPane;
    @FXML
    private ContextMenu listContextMenu;
    @FXML
    private ToggleButton filterToggleButton;
    @FXML
    private FilteredList<ToDoItem> filteredList;

    public void initialize() {

        listContextMenu = new ContextMenu();
        MenuItem deleteMenuItem = new MenuItem("Delete");
        deleteMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                ToDoItem item = todoListView.getSelectionModel().getSelectedItem();
                deleteItem(item);
            }
        });
        listContextMenu.getItems().addAll(deleteMenuItem);
        todoListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<ToDoItem>() {
            @Override
            public void changed(ObservableValue<? extends ToDoItem> observableValue, ToDoItem oldValue, ToDoItem newValue) {
                if(newValue != null) {
                    ToDoItem item = todoListView.getSelectionModel().getSelectedItem();
                    itemDetails.setText(item.getDetails());
                    DateTimeFormatter df = DateTimeFormatter.ofPattern("MMMM d, yyyy");
                    deadlineLabel.setText("Due: " + df.format(item.getDeadline()));
                }
            }
        });

        filteredList = new FilteredList<>(ToDoData.getInstance().getToDoItems(),
                new Predicate<ToDoItem>() {
                    @Override
                    public boolean test(ToDoItem item) {
                        return true;
                    }
                });

        SortedList<ToDoItem> sortedList = new SortedList<>(filteredList,
                new Comparator<ToDoItem>() {

                    @Override
                    public int compare(ToDoItem o1, ToDoItem o2) {
                        return o1.getDeadline().compareTo(o2.getDeadline());
                    }
                });

        todoListView.setItems(sortedList);
        todoListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        todoListView.getSelectionModel().selectFirst();

        todoListView.setCellFactory(new Callback<ListView<ToDoItem>, ListCell<ToDoItem>>() {
            @Override
            public ListCell<ToDoItem> call(ListView<ToDoItem> toDoItemListView) {
                ListCell<ToDoItem> cell = new ListCell<ToDoItem>() {

                    @Override
                    protected void updateItem(ToDoItem toDoItem, boolean b) {
                        super.updateItem(toDoItem, b);
                        if(b) {
                            setText(null);
                        } else {
                            setText(toDoItem.getShortDescription());
                            if (toDoItem.getDeadline().isBefore(LocalDate.now().plusDays(1))) {
                                setTextFill(Color.RED);
                            } else if (toDoItem.getDeadline().equals(LocalDate.now().plusDays(1))) {
                                setTextFill(Color.GREEN);
                            }
                        }
                    }
                };
                cell.emptyProperty().addListener(
                        (observableValue, aBoolean, t1) -> {
                            if (t1) {
                                cell.setContextMenu(null);
                            } else {
                                cell.setContextMenu(listContextMenu);
                            }
                        }
                );
                return cell;
            }
        });
    }

    public void showNewItemDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(mainBorderPane.getScene().getWindow());
        dialog.setTitle("Add New Event");
        dialog.setHeaderText("Use this windows to insert new Event");
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("todoItemDialog.fxml"));
        try {
            dialog.getDialogPane().setContent(fxmlLoader.load());
        } catch (IOException e) {
            System.out.println("Couldn't load the dialog");
            e.printStackTrace();
            return;
        }
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            TodoItemDialogController controller = fxmlLoader.getController();
            ToDoItem newItem = controller.processResults();
            todoListView.getSelectionModel().select(newItem);
        }

    }

    @FXML
    public void handleClickListView() {

        ToDoItem item = todoListView.getSelectionModel().getSelectedItem();
    }

    public void deleteItem(ToDoItem item) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete the event");
        alert.setHeaderText("Delete item: " + item.getShortDescription());
        alert.setContentText("Are you sure? Press OK to confirm, Cancel to return");
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            ToDoData.getInstance().deleteToDoItem(item);
        }
    }

    @FXML
    public void handleKeyPressed(KeyEvent deleteKey) {
        ToDoItem selectedItem = todoListView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            if(deleteKey.getCode().equals(KeyCode.BACK_SPACE)) {
                deleteItem(selectedItem);
            }
        }
    }

    @FXML
    public void handleFilterButton() {
        if(filterToggleButton.isSelected()) {
            filteredList.setPredicate(new Predicate<ToDoItem>() {
                @Override
                public boolean test(ToDoItem item) {
                    return item.getDeadline().equals(LocalDate.now());
                }
            });
        } else {
            filteredList.setPredicate(new Predicate<ToDoItem>() {
                @Override
                public boolean test(ToDoItem item) {
                    return true;
                }
            });
        }
    }

    @FXML
    public void handleExit() {
        Platform.exit();
    }
}
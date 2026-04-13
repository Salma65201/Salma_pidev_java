package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import tn.esprit.entities.Jeu;
import tn.esprit.services.ServiceJeu;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class JeuxController implements Initializable {

    @FXML
    private TableView<Jeu> jeuxTable;
    @FXML
    private TableColumn<Jeu, Integer> idCol;
    @FXML
    private TableColumn<Jeu, String> nomCol;
    @FXML
    private TableColumn<Jeu, String> genreCol;
    @FXML
    private TableColumn<Jeu, String> plateformeCol;
    @FXML
    private TableColumn<Jeu, String> statutCol;
    @FXML
    private TableColumn<Jeu, String> descCol;
    @FXML
    private TableColumn<Jeu, String> actionsCol;
    @FXML
    private Label messageLabel;
    @FXML
    private Label totalJeuxLabel;

    private final ServiceJeu serviceJeu = new ServiceJeu();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupColumns();
        loadJeux();
    }

    private void setupColumns() {
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        nomCol.setCellValueFactory(new PropertyValueFactory<>("nom"));
        genreCol.setCellValueFactory(new PropertyValueFactory<>("genre"));
        plateformeCol.setCellValueFactory(new PropertyValueFactory<>("plateforme"));
        statutCol.setCellValueFactory(new PropertyValueFactory<>("statut"));
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));

        actionsCol.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Modifier");
            private final Button deleteBtn = new Button("Supprimer");
            private final HBox box = new HBox(8, editBtn, deleteBtn);

            {
                editBtn.setStyle("-fx-background-color: #f59e0b; -fx-text-fill: white; -fx-cursor: hand;");
                deleteBtn.setStyle("-fx-background-color: #dc2626; -fx-text-fill: white; -fx-cursor: hand;");

                editBtn.setOnAction(e -> {
                    Jeu j = getTableView().getItems().get(getIndex());
                    openEditWindow(j);
                });

                deleteBtn.setOnAction(e -> {
                    Jeu j = getTableView().getItems().get(getIndex());
                    confirmDelete(j);
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private void loadJeux() {
        try {
            List<Jeu> list = serviceJeu.getAll();
            jeuxTable.setItems(FXCollections.observableArrayList(list));
            totalJeuxLabel.setText(String.valueOf(list.size()));
            messageLabel.setText("");
        } catch (SQLException e) {
            showError("Impossible de charger les jeux : " + e.getMessage());
        }
    }

    @FXML
    private void handleNewJeu() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ajouterJeu.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Ajouter un jeu");
            stage.setScene(new Scene(root));
            stage.showAndWait();
            loadJeux();
        } catch (IOException e) {
            showError("Erreur d'ouverture du formulaire : " + e.getMessage());
        }
    }

    private void openEditWindow(Jeu j) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ajouterJeu.fxml"));
            Parent root = loader.load();
            AjouterJeuController controller = loader.getController();
            controller.setJeuToEdit(j);
            Stage stage = new Stage();
            stage.setTitle("Modifier le jeu");
            stage.setScene(new Scene(root));
            stage.showAndWait();
            loadJeux();
        } catch (IOException e) {
            showError("Erreur d'ouverture du formulaire : " + e.getMessage());
        }
    }

    private void confirmDelete(Jeu j) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText(null);
        confirm.setContentText("Supprimer le jeu « " + j.getNom() + " » ?");
        confirm.showAndWait().ifPresent(button -> {
            if (button == ButtonType.OK) {
                try {
                    serviceJeu.supprimer(j.getId());
                    messageLabel.setStyle("-fx-text-fill: #4ade80;");
                    messageLabel.setText("Jeu supprimé.");
                    loadJeux();
                } catch (SQLException ex) {
                    showError("Suppression impossible : " + ex.getMessage());
                }
            }
        });
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Erreur");
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}

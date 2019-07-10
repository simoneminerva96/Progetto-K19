package Client.Controller;


import Server.Domain.Auction;
import Client.Domain.ClientManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.effect.BoxBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;


public class AuctionListController extends TemplateController {
    private ArrayList<Auction> Alist;

    @FXML
    private ListView<Auction> auctionList;

    private final ObservableList<Auction> auction = FXCollections.observableArrayList();

    private AuctionListController auctionListController;

    private CreateAuctionFormController auctionFormController;

    private TitleController titleController;



    public void initializeList(int typeOfSearch, String toSearch) {
        Alist = null;
        auction.clear();
        //Richiedo al server una lista di 10 aste al massimo, successivamente posso chiedere di prenderne altre 10
        if(typeOfSearch == 0) {
            try {
                Alist = client.requestListAuction();
                for(Auction a: Alist){
                    System.out.println("Nome"+a.getId()+ "         path: "+a.getImage().getAbsolutePath());
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        else if (typeOfSearch == 1) {
            try {
                Alist = client.searchAuction(toSearch);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        else if (typeOfSearch == 2) {
            try {
                Alist = client.requestFavoriteAuction();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        else if (typeOfSearch == 3) {
            try {
                Alist = client.myAuction();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        if(Alist != null) {
            for (int i = 0; i < Alist.size(); i++) {
                auction.add(Alist.get(i));
            }

            auctionList.setCellFactory(new Callback<ListView<Auction>, ListCell<Auction>>() {
                @Override
                public ListCell<Auction> call(ListView<Auction> param) {
                    ListCell<Auction> cell = new ListCell<Auction>() {
                        Image img;
                        ImageView imgview = null;

                        protected void updateItem(Auction au, boolean bt1) {
                            super.updateItem(au,bt1);
                            if(bt1)
                                setStyle("-fx-background-color: #81c784"); // Da togliere se si vuole lo stacco
                            if(au != null) {
                                if(au.getImage().exists()) {
                                    try {
                                        img = new Image(new FileInputStream(au.getImage()),100,100,false,false);

                                        imgview = new ImageView();
                                        imgview.setImage(img);
                                    }catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                                else {
                                    try {
                                        File file = null;
                                        try {
                                            URL res = getClass().getClassLoader().getResource("Images/i_have_no_idea.png");
                                            file = Paths.get(res.toURI()).toFile();
                                        }catch (URISyntaxException e) {
                                            e.printStackTrace();
                                        }
                                        //In alternativa a tutto quello sopra a partire dal try si puo' usare questo path: target/classes/Images/i_have_no_idea.png
                                        String absolutePath = file.getAbsolutePath();
                                        img = new Image(new FileInputStream(absolutePath), 100, 100, false, false);

                                        imgview = new ImageView();
                                        imgview.setImage(img);
                                    } catch (FileNotFoundException e) {
                                        e.printStackTrace();
                                    }
                                }

                                setGraphic(imgview);
                                setText("Id: " + Integer.toString(au.getId()) + "\t\t\t" + "Name: " + au.getLot().getDescription() + "\t\t\t\t " + "Value: " + Integer.toString(au.getHigherOffer()) +  "\n\t\t\t\t" + "Close Date: " + parseDate(au.getClosingDate()));

                                setStyle("-fx-background-color: #81c784"); //Da togliere se si vuole lo stacco
                            }
                        }
                    };
                    return cell;
                }
            });
            auctionList.setItems(auction);
        }
    }

    public String parseDate(LocalDateTime closingTime) {

        return closingTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")).toString();
    }



    void refreshList() {
        //Per un bug visuale se non ricarico la Lista andando ad aggiornare solo l'observable list si buggano le immagini, probabilmente visto che uso una custom list cell
        initializeList(0,null);
    }


    public void searchList(String toSearch) {
        //Usato per cercare
        initializeList(1,toSearch);
    }

    public void loadFavorite() {
        //Per un bug visuale se non ricarico la Lista andando ad aggiornare solo l'observable list si buggano le immagini, probabilmente visto che uso una custom list cell
        initializeList(2,null);
    }

    public void loadMyAuction() {
        //Per un bug visuale se non ricarico la Lista andando ad aggiornare solo l'observable list si buggano le immagini, probabilmente visto che uso una custom list cell
        initializeList(3,null);
    }

    @FXML
    public void chooseAuction() throws IOException {
        try {

            int idChoose = auctionList.getSelectionModel().getSelectedItem().getId();

            if(client.isClosed(idChoose) && client.getAuction(idChoose).getLastBid()!= null) {
                if (client.getAuction(idChoose).getLastBid().getActorDBUsername().equals(client.getLoggedUser())) { // se l'utente e' il vincitore
                    Stage popUpStageWinnerCard = loadScenePopUp("/View/AuctionWinnerCard.fxml");

                    ((AuctionWinnerCardController)loader.getController()).setPopUpStage(popUpStageWinnerCard);
                    ((AuctionWinnerCardController) loader.getController()).setAuction(client.getAuction(idChoose));
                    ((AuctionWinnerCardController) loader.getController()).setClient(client);
                    ((AuctionWinnerCardController) loader.getController()).initializeNow();

                    popUpStageWinnerCard.show();
                }
            }
            else if ((!client.isClosed(idChoose) || titleController.getMyAuction().isDisable() || titleController.getFavoriteButton().isDisable()) || (client.isClosed(idChoose) && !(client.getAuction(idChoose).getLastBid().getActorDBUsername().equals(client.getLoggedUser())))) {
                Stage popUpStageAuctionCard = loadScenePopUp("/View/AuctionCard.fxml");

                ((AuctionCardController)loader.getController()).setPopUpStage(popUpStageAuctionCard);

                ((AuctionCardController) loader.getController()).setAuction(client.getAuction(idChoose));
                ((AuctionCardController) loader.getController()).setClient(client);
                ((AuctionCardController) loader.getController()).initializeNow();
                ((AuctionCardController) loader.getController()).setAuctionFormController(auctionFormController);
                ((AuctionCardController) loader.getController()).initializeWindow();
                popUpStageAuctionCard.show();

            }
            else {
                initializeList(0, null);
            }
        }catch (NullPointerException e) {
            initializeList(0, null);
        }

    }




    public AuctionListController getAuctionListController() {
        return auctionListController;
    }

    public void setAuctionListController(AuctionListController auctionListController) {
        this.auctionListController = auctionListController;
    }

    public CreateAuctionFormController getAuctionFormController() {
        return auctionFormController;
    }

    public void setAuctionFormController(CreateAuctionFormController auctionFormController) {
        this.auctionFormController = auctionFormController;
    }

    public TitleController getTitleController() {
        return titleController;
    }

    public void setTitleController(TitleController titleController) {
        this.titleController = titleController;
    }
}

package InGame.Utility;

import InGame.EPlayer;
import InGame.GameInstance;
import InGame.Monster.Monster;
import InGame.Element;

public class BoardUpdatePair {

    private final PlayerSpecific<PlayerBoardUpdate> updates = new PlayerSpecific<>(new PlayerBoardUpdate(), new PlayerBoardUpdate());
    private final PlayerSpecific<PlayerBoardUpdate> fullBoards = new PlayerSpecific<>(new PlayerBoardUpdate(), new PlayerBoardUpdate());

    public BoardUpdatePair(){ }

    public PlayerBoardUpdate getAndReset(EPlayer player){
        PlayerBoardUpdate result = updates.get(player);
        updates.set(new PlayerBoardUpdate(), player);
        return result;
    }
    public PlayerBoardUpdate getFullBoardAndReset(EPlayer player){
        updates.set(new PlayerBoardUpdate(), player);
        return fullBoards.get(player);
    }
    public void setElements(Element elementPlayerOne, Element elementPlayerTwo){
        updates.get(EPlayer.ONE).setElements(elementPlayerOne.databaseID, elementPlayerTwo.databaseID);
        updates.get(EPlayer.TWO).setElements(elementPlayerTwo.databaseID, elementPlayerOne.databaseID);
        fullBoards.get(EPlayer.ONE).setElements(elementPlayerOne.databaseID, elementPlayerTwo.databaseID);
        fullBoards.get(EPlayer.TWO).setElements(elementPlayerTwo.databaseID, elementPlayerOne.databaseID);
    }

    public void setPlayerTurn(EPlayer player){
        updates.get(player).isPlayerTurn = true;
        updates.get(player.other()).isPlayerTurn = false;
        fullBoards.get(player).isPlayerTurn = true;
        fullBoards.get(player.other()).isPlayerTurn = false;
    }
    public void setRoundMana(int roundMana){
        updates.get(EPlayer.ONE).roundMana = roundMana;
        updates.get(EPlayer.TWO).roundMana = roundMana;
        fullBoards.get(EPlayer.ONE).roundMana = roundMana;
        fullBoards.get(EPlayer.TWO).roundMana = roundMana;
    }
    public void setPlayerHealth(EPlayer player, int health){
        updates.get(player).playerHealth = health;
        updates.get(player.other()).opponentHealth = health;
        fullBoards.get(player).playerHealth = health;
        fullBoards.get(player.other()).opponentHealth = health;
    }
    public void setPlayerMana(EPlayer player, int mana){
        updates.get(player).playerMana = mana;
        updates.get(player.other()).opponentMana = mana;
        fullBoards.get(player).playerMana = mana;
        fullBoards.get(player.other()).opponentMana = mana;

    }
    public void setDeckSize(EPlayer player, int deckSize){
        updates.get(player).playerDeckSize = deckSize;
        updates.get(player.other()).opponentDeckSize = deckSize;
        fullBoards.get(player).playerDeckSize = deckSize;
        fullBoards.get(player.other()).opponentDeckSize = deckSize;
    }
    public void removeHandCard(EPlayer player, int handCardID){
        updates.get(player).removeHandCard(handCardID);
        updates.get(player.other()).opponentHandDiff -= 1;
        fullBoards.get(player).removeHandCard(handCardID);
        fullBoards.get(player.other()).opponentHandDiff -= 1;
    }
    public void addHandCard(EPlayer player, GameInstance.HandCard card ){
        PlayerBoardUpdate.HandCard newCard = new PlayerBoardUpdate.HandCard(card.cardType.typeID, card.imageID);
        updates.get(player).addHandCard(card.identifier, newCard);
        updates.get(player.other()).opponentHandDiff += 1;
        fullBoards.get(player).addHandCard(card.identifier, newCard);
        fullBoards.get(player.other()).opponentHandDiff += 1;
    }
    public void addMonster(Monster monster, BoardPosition position){
        PlayerBoardUpdate.AddedMonster mon = new PlayerBoardUpdate.AddedMonster(monster.typeID, monster.getImageID(), monster.getCurrentHealth(), monster.getCurrentDamage(), monster.isStunned());
        updates.get(EPlayer.ONE).addMonster(getRelativePosition(position, EPlayer.ONE), mon);
        updates.get(EPlayer.TWO).addMonster(getRelativePosition(position, EPlayer.TWO), mon);
        fullBoards.get(EPlayer.ONE).addMonster(getRelativePosition(position, EPlayer.ONE), mon);
        fullBoards.get(EPlayer.TWO).addMonster(getRelativePosition(position, EPlayer.TWO), mon);
    }
    public void removeMonster(BoardPosition position){
        updates.get(EPlayer.ONE).removeMonster(getRelativePosition(position, EPlayer.ONE));
        updates.get(EPlayer.TWO).removeMonster(getRelativePosition(position, EPlayer.TWO));
        fullBoards.get(EPlayer.ONE).removeMonster(getRelativePosition(position, EPlayer.ONE));
        fullBoards.get(EPlayer.TWO).removeMonster(getRelativePosition(position, EPlayer.TWO));
    }
    public void updateMonsterHealth(BoardPosition position, int health){
        PlayerBoardUpdate.MonsterUpdate update = PlayerBoardUpdate.MonsterUpdate.healthUpdate(health);
        updates.get(EPlayer.ONE).updateMonster(getRelativePosition(position, EPlayer.ONE), update);
        updates.get(EPlayer.TWO).updateMonster(getRelativePosition(position, EPlayer.TWO), update);
        fullBoards.get(EPlayer.ONE).updateMonster(getRelativePosition(position, EPlayer.ONE), update);
        fullBoards.get(EPlayer.TWO).updateMonster(getRelativePosition(position, EPlayer.TWO), update);
    }
    public void updateMonsterDamage(BoardPosition position, int damage){
        PlayerBoardUpdate.MonsterUpdate update = PlayerBoardUpdate.MonsterUpdate.damageUpdate(damage);
        updates.get(EPlayer.ONE).updateMonster(getRelativePosition(position, EPlayer.ONE), update);
        updates.get(EPlayer.TWO).updateMonster(getRelativePosition(position, EPlayer.TWO), update);
        fullBoards.get(EPlayer.ONE).updateMonster(getRelativePosition(position, EPlayer.ONE), update);
        fullBoards.get(EPlayer.TWO).updateMonster(getRelativePosition(position, EPlayer.TWO), update);
    }
    public void updateMonsterStunned(BoardPosition position, boolean stunned){
        PlayerBoardUpdate.MonsterUpdate update = PlayerBoardUpdate.MonsterUpdate.stunUpdate(stunned);
        updates.get(EPlayer.ONE).updateMonster(getRelativePosition(position, EPlayer.ONE), update);
        updates.get(EPlayer.TWO).updateMonster(getRelativePosition(position, EPlayer.TWO), update);
        fullBoards.get(EPlayer.ONE).updateMonster(getRelativePosition(position, EPlayer.ONE), update);
        fullBoards.get(EPlayer.TWO).updateMonster(getRelativePosition(position, EPlayer.TWO), update);
    }

    private PlayerBoardUpdate.PlayerBoardPosition getRelativePosition(BoardPosition pos, EPlayer player){
        return new PlayerBoardUpdate.PlayerBoardPosition(pos.getSlotID(), pos.getSide() != player);
    }
}
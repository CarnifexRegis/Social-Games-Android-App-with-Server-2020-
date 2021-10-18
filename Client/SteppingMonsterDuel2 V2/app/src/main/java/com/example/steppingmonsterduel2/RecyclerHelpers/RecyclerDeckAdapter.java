package com.example.steppingmonsterduel2.RecyclerHelpers;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.steppingmonsterduel2.CustomizeDeck;
import com.example.steppingmonsterduel2.Objects.PlayerCard;
import com.example.steppingmonsterduel2.R;
import com.example.steppingmonsterduel2.Services.Element;
import com.example.steppingmonsterduel2.Services.GameContent;
import com.example.steppingmonsterduel2.Util.Configuration;

import java.util.ArrayList;
/*
Obsolete Java file
Was kept to be reused in CustomizeDeckActivity, which is the updated version
 */
public class RecyclerDeckAdapter extends RecyclerView.Adapter<RecyclerDeckAdapter.SpellHolder>{
    private static final String TAG = "RecyclerViewAdapter";
    public ArrayList<PlayerCard> cards;
   private Context context = null;
   // true for deck false for cards
    boolean type;
    CustomizeDeck customizeDeck;
    public RecyclerDeckAdapter (Context c, ArrayList<PlayerCard> cards, CustomizeDeck customizeDeck, boolean type){
        super();
        context = c;
        this.cards = cards;
        this.type = type;
        this.customizeDeck = customizeDeck;

    }
    @NonNull

    @Override
    public SpellHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        Log.d(TAG, "Creating View holder");
        View view = null;
        switch (viewType){
            case 0:
                view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_layout_monster,viewGroup,false);
                return new MonsterHolder(view);

            case 1:
                view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_layout_spell,viewGroup,false);
                return new SpellHolder(view);

                default:
                    view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_layout_spell,viewGroup,false);
                    return new SpellHolder(view);
        }
    }
    public void addCard(PlayerCard card, int position){
        cards.add(card);
        notifyItemInserted(cards.size());
        notifyItemRangeInserted(cards.size(),1);
      //  notifyDataSetChanged();
    }
    @Override
    public void onBindViewHolder(@NonNull SpellHolder cardHolder, int position) {
      switch(cardHolder.getItemViewType()){

          case 0://monster
             MonsterHolder mh = (MonsterHolder)cardHolder;
              GameContent.MonsterType mt= (GameContent.MonsterType) cards.get(position).getCardType();
              Log.d(TAG, "trying Strings");
              mh.name.setText(""+mt.name);
              mh.mana.setText(""+mt.mana);
              mh.atk.setText(""+mt.damage);
              mh.def.setText(""+mt.health);
              Log.d(TAG, "trying Picture");
              Configuration.getPictureOutOfStorageAndSetItToView( context, mh.customPicture, cards.get(position).getPicture());
              Log.d(TAG, "Binded Moster Holder");
              mh.itemView.setOnClickListener(new View.OnClickListener() {

                  @Override
                  public void onClick(View arg0) {
                      mh.itemView.setClickable(false);
                        if(type){
                            if(customizeDeck.removeCardFromDeck(customizeDeck.idOfDeck,cards.get(position).getCardID())){
                            //  customizeDeck.getOwnedAdapter().addCard(cards.get(position),position);
                                cardHolder. removeCard(position);
                                notifyItemRemoved(cardHolder.getAdapterPosition());
                                notifyItemRangeRemoved(cardHolder.getAdapterPosition(),1);
                             //   notifyDataSetChanged();
                            }else{
                                Toast.makeText(customizeDeck,
                                        "Something went wrong removing Card from Deck",Toast.LENGTH_SHORT).show();
                            }
                        }else{
                            if(customizeDeck.getDeckAdapter().cards.size()<15&&customizeDeck.addCardToDeck(customizeDeck.idOfDeck,cards.get(position).getCardID())){
                                customizeDeck.getDeckAdapter().addCard(cards.get(position),position);
                            }else {
                                if(customizeDeck.getDeckAdapter().cards.size()<15){
                                Toast.makeText(customizeDeck,
                                        "You can´t add more than 15 cards per Deck",Toast.LENGTH_SHORT).show();
                                }else{
                                    Toast.makeText(customizeDeck,
                                            "You can´t add the same card instance more than once",Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                      mh.itemView.setClickable(true);
                  }

              });
              break;
          case 1://spell
              SpellHolder sh = (SpellHolder) cardHolder;
              GameContent.CardType st= (GameContent.CardType)cards.get(position).getCardType();
              sh.name.setText(st.name);
              sh.mana.setText(""+st.mana);
              sh.description.setText(st.descriptionLong);
              Configuration.getPictureOutOfStorageAndSetItToView( context, sh.customPicture, cards.get(position).getPicture());
              Log.d(TAG, "Binded Spell Holder");
              sh.itemView.setOnClickListener(new View.OnClickListener() {

                  @Override
                  public void onClick(View arg0) {
                      if(type){
                          if(customizeDeck.removeCardFromDeck(customizeDeck.idOfDeck,cards.get(position).getCardID())){
                               cardHolder. removeCard(position);
                              notifyItemRemoved(cardHolder.getAdapterPosition());
                              notifyItemRangeRemoved(cardHolder.getAdapterPosition(),1);
                             // notifyDataSetChanged();
                          }else{
                              Toast.makeText(customizeDeck,
                                      "Something went wrong removing Card from Deck",Toast.LENGTH_SHORT).show();
                          }
                      }else{
                          if(customizeDeck.getDeckAdapter().cards.size()<15&&customizeDeck.addCardToDeck(customizeDeck.idOfDeck,cards.get(position).getCardID())){
                              customizeDeck.getDeckAdapter().addCard(cards.get(position),position);
                          }else {
                              Toast.makeText(customizeDeck,
                                      "Something went wrong adding Card to Deck you can´t add more than 15 cards per Deck",Toast.LENGTH_SHORT).show();
                          }
                      }
                  }

              });
              break;
              default: //spell
                  break;
      }
    }
    @Override
    public int getItemViewType(int position){
        if(cards.get(position).getCardType().getClass()== new GameContent.MonsterType("retarded", Element.EARTHQUAKE,1,2,3,1,"ashell", "ashell").getClass()){ //WTF are you doing here? -Maxi
            return 0;
        }else{
            return 1;
        }

    }

    @Override
    public int getItemCount() {
        return cards.size();
    }




    public class SpellHolder extends RecyclerView.ViewHolder  {
      TextView name;
      TextView mana;
      TextView description;
      ImageView customPicture;

      public SpellHolder(@NonNull View itemView) {
          super(itemView);
          try{
              // in case of cancer
          name =  itemView.findViewById(R.id.name);
          mana= itemView.findViewById(R.id.mana);
          description = itemView.findViewById(R.id.description);
              Log.d(TAG, "Instanciating spell holder");
          customPicture = itemView.findViewById(R.id.image);}catch (Exception e){
              Log.d(TAG, "weird interactions between subclasses");
          }

      }
        public void addCard(PlayerCard card, int position){
            cards.add(card);
            notifyItemInserted(getAdapterPosition());
            notifyItemRangeChanged(getAdapterPosition(),cards.size());
        }
        public void removeCard(int position){
            cards.remove(getAdapterPosition());
            notifyItemRemoved(getAdapterPosition());
            notifyItemRangeChanged(getAdapterPosition(),cards.size());

        }
  }


  public class MonsterHolder extends  SpellHolder {
        TextView name;
        TextView mana;
        TextView atk;
        TextView def;
        ImageView customPicture;
        public MonsterHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            mana = itemView.findViewById(R.id.mana);
            atk = itemView.findViewById(R.id.damage);
            def = itemView.findViewById(R.id.health);
            customPicture = itemView.findViewById(R.id.image);
            Log.d(TAG, "Instanciating monster holder");
        }
    }
}

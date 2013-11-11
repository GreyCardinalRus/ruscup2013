import model.*;

import java.util.Random;

public final class MyStrategy implements Strategy {
	private final Random random = new Random();
	 Bonus  moveToBonus = null;
	Trooper myEnimy = null;
	private Unit myMove(Unit target,Trooper self, World world, Move move) {
		int newX=self.getX(),newY=self.getY();
		
//		System.out.println("-=!=- target : " + " X="+target.getX()+" Y="+target.getY()+" id="+target.getId());
		if(target.getY()!=self.getY())
		{   newY+=(target.getY()<self.getY() ? -1	: 1)	;
			//move.setDirection(target.getY()<self.getY() ? Direction.NORTH
			//		: Direction.SOUTH	);
	//	System.out.println("newX="+newX+" newY="+newY);
			if(world.getCells()[newX][newY]!=CellType.FREE) newY=self.getY();
		}
		
		if(newY==self.getY()&&target.getX()!=self.getX())
		{
			newX+=target.getX()<self.getX() ? -1	: 1	;
			//move.setDirection(target.getX()<self.getX() ? Direction.WEST
			//		: Direction.EAST
			//		);	
			if(world.getCells()[newX][newY]!=CellType.FREE) newX=self.getX();
			//if(world.getCells()[self.getX()+(target.getX()<self.getX() ?-1:1)][self.getY()]!=CellType.FREE) move.setDirection(null);
			//if(world.getCells()[move.getX()][move.getY()]!=CellType.FREE) move.setDirection(null);
		}
		if(target.getY()==newY&&target.getX()==newX) target = null;
		move.setX(newX);move.setY(newY);
		//if(world.getCells()[move.getX()][move.getY()]==CellType.FREE)
		//else 
//		System.out.println("-=!=- Direction : " +move.getDirection()+" target="+target);
		return target;
		//move.setX(moveToBonus.getX());
		//move.setY(moveToBonus.getY());
	
	}
	@Override
	public void move(Trooper self, World world, Game game, Move move) {
		if (self.getActionPoints() < 1) {// game.getStandingMoveCost()) {
			 moveToBonus = null;
			return;
		}
		if(self.getMaximalHitpoints()>self.getHitpoints()&&self.isHoldingMedikit()&&self.getActionPoints()>game.getMedikitUseCost())
		{
			move.setAction(ActionType.USE_MEDIKIT);
			return;
		}
			
		Trooper[] trooreps = world.getTroopers();
		Player[] players = world.getPlayers();
		Bonus[] bonuses = world.getBonuses();

		boolean haveVisibleenemys = false;

		
//		System.out.println("-=Why I am = " + self.getType()+" X="+self.getX()+" Y="+self.getY() + " : "
//				+ self.getActionPoints());
		if( moveToBonus == null) 
		for (int i = 0; i < bonuses.length; i++) {
			// if (players[pid].getId()==trooreps[i].getPlayerId())
			Bonus  foundBonus = null;
			// System.out.println(bonuses[i].getType());
			if (bonuses[i].getType() == BonusType.FIELD_RATION
					&& !self.isHoldingFieldRation()) {
				foundBonus = bonuses[i];
			} else if (bonuses[i].getType() == BonusType.GRENADE
					&& !self.isHoldingGrenade()) {
				foundBonus = bonuses[i];
			} else if (bonuses[i].getType() == BonusType.MEDIKIT
					&& !self.isHoldingMedikit()) {
				foundBonus = bonuses[i];
			}
            if(foundBonus!=null && moveToBonus == null) moveToBonus= foundBonus;
            else if(foundBonus==null) ;
            else if(self.getDistanceTo(moveToBonus)>self.getDistanceTo(foundBonus)) moveToBonus= foundBonus;
		}
	//	if (!(moveToBonus == null))
	//		System.out.println("Bonus = " + moveToBonus.getType());

		// System.out.println("	-=Svoy");
		for (int i = 0; i < trooreps.length; i++) {
			// if (players[pid].getId()==trooreps[i].getPlayerId())

			if (trooreps[i].isTeammate() && trooreps[i].getId() != self.getId()) {
				// System.out.println(trooreps[i].getType());
			}

		}

		for (int i = 0; i < trooreps.length; i++) {
			// if (players[pid].getId()==trooreps[i].getPlayerId())

			if (!trooreps[i].isTeammate()) {
				haveVisibleenemys = true;
				// System.out.println(trooreps[i].getType());
			}

		}
		if (haveVisibleenemys)
			System.out.println("	-=Cysie");
		for (int i = 0; i < trooreps.length; i++) {
			// if (players[pid].getId()==trooreps[i].getPlayerId())

			if (!trooreps[i].isTeammate()) {
				myEnimy = trooreps[i];
				// haveVisibleenemys =true;
				System.out.println(trooreps[i].getType());
			}
		}
		if (haveVisibleenemys) {
			if( self.getShootCost()<self.getActionPoints() && world.isVisible(self.getShootingRange(), self.getX(), self.getY(), self.getStance(), myEnimy.getX(), myEnimy.getY(), myEnimy.getStance()))
			{
				move.setAction(ActionType.SHOOT);
				move.setX(myEnimy.getX());move.setY(myEnimy.getY());
			}
			else{
				move.setAction(ActionType.MOVE);
				myEnimy= (Trooper) myMove(myEnimy,self,world, move);
			}
			//move.setAction(ActionType.MOVE);

			//if (random.nextBoolean()) {
			//	move.setDirection(random.nextBoolean() ? Direction.WEST
			//			: Direction.EAST);
			//} else {
			//	move.setDirection(random.nextBoolean() ? Direction.SOUTH
			//			: Direction.NORTH);
			//}
		} else // freestyle
		{
			
			if (!(moveToBonus == null))
			{
				move.setAction(ActionType.MOVE);
				moveToBonus=(Bonus) myMove(moveToBonus,self,world, move);
				//System.out.println("Self = " + moveToBonus.getType()+"="+moveToBonus.getX()+" Y="+moveToBonus.getY());
//				System.out.println("-=!=- Bonus : " + moveToBonus.getType()+" X="+moveToBonus.getX()+" Y="+moveToBonus.getY()+" id="+moveToBonus.getId());
//				if(moveToBonus.getX()==self.getX())
//				{
//					move.setDirection(moveToBonus.getY()<self.getY() ? Direction.NORTH
//							: Direction.SOUTH	);
//				}
//				else if(moveToBonus.getY()==self.getY()) moveToBonus = null;
//				else
//				{
//					move.setDirection(moveToBonus.getX()<self.getX() ? Direction.WEST
//							: Direction.EAST
//							);	
//				}
//				System.out.println("-=!=- Direction : " +move.getDirection());
//				//move.setX(moveToBonus.getX());
//				//move.setY(moveToBonus.getY());
			
			}
		// get location

		}
	}
}

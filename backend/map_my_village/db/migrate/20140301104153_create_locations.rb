class CreateLocations < ActiveRecord::Migration
  def change
    create_table :locations do |t|
      t.integer :project_id
      t.decimal :latitude
      t.decimal :longitude

      t.timestamps
    end
  end
end

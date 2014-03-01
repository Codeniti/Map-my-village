class LocationController < ApplicationController
  def create
  	# @location = Location.new
  	data = JSON.parse(params[:data])
  	Location.create(project_id: data["project_id"],latitude: data["lat"],longitude: data["long"])
  	render json: {status: true} 
  end
end

class ProjectController < ApplicationController

  def index
  	@projects = Project.all
  end

  def show
  	@project = Project.find(params[:id])
  	@lats = @project.locations
  end

  def create
  	@project = Project.create(name: params[:name])
    @location = @project.locations
  	render :text => @project.id
  end

end
